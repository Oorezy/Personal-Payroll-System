package com.introtech.authenticationresource.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class JwtConfig {

    @Bean
    JwtDecoder jwtDecoder(SecurityProperties properties) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri())
                .build();


        OAuth2TokenValidator<Jwt> issuerValidator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                jwt -> {
                    if (!"access-token".equals(jwt.getClaimAsString("typ")))
                        return OAuth2TokenValidatorResult.failure(new OAuth2Error("Invalid token type"));
                    else
                        return OAuth2TokenValidatorResult.success();
                },
        jwt -> {
            if (jwt.getAudience() != null && jwt.getAudience().contains(properties.clientName()))
                return OAuth2TokenValidatorResult.success();
            else
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("Invalid client name/audience"));
        });

        decoder.setJwtValidator(issuerValidator);
        return decoder;
    }
}
