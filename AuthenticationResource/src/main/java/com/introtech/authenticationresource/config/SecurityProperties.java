package com.introtech.authenticationresource.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties("introtech.security")
public record SecurityProperties(String jwkSetUri,
                                 Set<String> unSecurePaths,
                                 String clientName) {
}
