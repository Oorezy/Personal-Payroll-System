package com.introtech.authenticationservice.jwt;

import com.introtech.authenticationservice.UserDetailsImpl;
import com.introtech.authenticationservice.entity.RefreshTokens;
import com.introtech.authenticationservice.service.AuthUserService;
import com.introtech.authenticationservice.service.RefreshTokenService;
import com.nimbusds.jose.JOSEObjectType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.introtech.authenticationservice.jwt.SecurityResource.ACCESS_TOKEN_TYPE;
import static com.introtech.authenticationservice.jwt.SecurityResource.JWT_TYPE_CLAIM;
import static com.introtech.authenticationservice.jwt.SecurityResource.REFRESH_TOKEN_TYPE;
import static com.introtech.authenticationservice.jwt.SecurityResource.ROLE_CLAIM;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final JwtEncoder jwtEncoder;
    private final AuthUserService authUserService;
    private final RefreshTokenService refreshTokenService;

    private static final ConcurrentHashMap<Long, UUID> lastUsedUserJtis = new ConcurrentHashMap<>();


    public JwtService(JwtProperties jwtProperties, JwtEncoder jwtEncoder, AuthUserService authUserService, RefreshTokenService refreshTokenService){

        this.jwtProperties = jwtProperties;
        this.jwtEncoder = jwtEncoder;
        this.authUserService = authUserService;
        this.refreshTokenService = refreshTokenService;
    }

    public boolean validateToken(String token) {
        return extractClaim(token, Claims::getExpiration).after(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtProperties.publicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public JwtTokenResponse generateToken(UserDetailsImpl userDetails) {
        return JwtTokenResponse.builder()
                .accessToken(generateAccessToken(userDetails))
                .refreshToken(generateRefreshToken(userDetails))
                .expiresIn(jwtProperties.accessTokenDuration().toSeconds())
                .build();
    }

    public String generateAccessToken(UserDetailsImpl userDetails) {

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(jwtProperties.keyId())
                .type(JOSEObjectType.JWT.toString())
                .build();

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(jwtProperties.accessTokenDuration()))
                .subject(userDetails.getId().toString())
                .claim(JWT_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .claim(ROLE_CLAIM, roles)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

    }

    public String generateRefreshToken(UserDetailsImpl userDetails) {

        UUID oldJti = lastUsedUserJtis.remove(userDetails.getId());
        RefreshTokens refreshToken = refreshTokenService.generateRefreshToken(
                userDetails, oldJti, jwtProperties.refreshTokenDuration());

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(jwtProperties.keyId())
                .type(JOSEObjectType.JWT.toString())
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(refreshToken.getJti().toString())
                .subject(refreshToken.getUserId().toString())
                .issuer(jwtProperties.issuer())
                .issuedAt(refreshToken.getCreatedAt())
                .expiresAt(refreshToken.getExpiresAt())
                .claim(JWT_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public boolean validateRefreshToken(String token) {

        Jwt jwt = refreshTokenDecoder().decode(token);
        UUID jti = UUID.fromString(jwt.getId());
        Long userId = Long.valueOf(jwt.getSubject());
        lastUsedUserJtis.put(userId, jti);
        return true;
    }

    public UserDetailsImpl getUserDetailsFromToken(String token) {
        Jwt jwt = refreshTokenDecoder().decode(token);
        return authUserService.findUserById(Long.valueOf(jwt.getSubject()));
    }

    JwtDecoder refreshTokenDecoder() {
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withPublicKey(jwtProperties.publicKey()).build();

        OAuth2TokenValidator<Jwt> issuerValidator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()),
                jwt -> {
                    if (!REFRESH_TOKEN_TYPE.equals(jwt.getClaimAsString(JWT_TYPE_CLAIM)))
                        return OAuth2TokenValidatorResult.failure(new OAuth2Error("Invalid token type"));
                    else
                        return OAuth2TokenValidatorResult.success();
                },
                jwt -> {
                    UUID jti = UUID.fromString(jwt.getId());
                    if (refreshTokenService.isRefreshTokenValid(jti))
                        return OAuth2TokenValidatorResult.success();
                    else
                        return OAuth2TokenValidatorResult.failure(new OAuth2Error("Invalid token revoked"));
                });

        decoder.setJwtValidator(issuerValidator);

        return decoder;
    }
}
