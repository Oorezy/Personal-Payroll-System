package com.introtech.authenticationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AppConfig {

//    public AuditorAware<String> auditorProvider() {
//        return new AuditorAwareImpl();
//    }
}
