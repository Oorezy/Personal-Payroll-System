package com.introtech.authenticationservice.jwt;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import static com.introtech.authenticationservice.jwt.SecurityResource.ACCESS_TOKEN_TYPE;
import static com.introtech.authenticationservice.jwt.SecurityResource.JWT_TYPE_CLAIM;

public class JwtTokenValidator implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {

        if (!ACCESS_TOKEN_TYPE.equals(token.getClaimAsString(JWT_TYPE_CLAIM)))
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("Invalid token type"));
        else
            return OAuth2TokenValidatorResult.success();
    }
}
