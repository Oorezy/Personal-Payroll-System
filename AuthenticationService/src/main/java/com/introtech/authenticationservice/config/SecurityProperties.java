package com.introtech.authenticationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties("introtech.security")
public record SecurityProperties(String jwkSetUri,
                                 Set<String> unSecuredPaths) {
}
