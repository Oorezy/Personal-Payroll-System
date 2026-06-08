package com.introtech.authenticationservice.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties("introtech.security.jwt")
public record JwtProperties(RSAPublicKey publicKey,
                            RSAPrivateKey privateKey,
                            String keyId,
                            String issuer,
                            @DefaultValue("30") @DurationUnit(ChronoUnit.MINUTES) Duration accessTokenDuration,
                            @DefaultValue("7") @DurationUnit(ChronoUnit.DAYS) Duration refreshTokenDuration) {
}
