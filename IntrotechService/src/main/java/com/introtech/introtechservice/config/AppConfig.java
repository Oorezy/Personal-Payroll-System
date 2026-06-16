package com.introtech.introtechservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@EnableJpaAuditing
@Configuration
public class AppConfig {

    @Primary
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    RestClient.Builder customClient() {
        return RestClient.builder();
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            String username = "System";
            Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
            if  (auth != null) {
                username = auth.getName();
            }
            return Optional.of(username);
        };
    }
}
