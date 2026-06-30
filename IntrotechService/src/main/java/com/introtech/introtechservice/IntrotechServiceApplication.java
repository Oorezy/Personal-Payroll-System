package com.introtech.introtechservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.introtech")
public class IntrotechServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntrotechServiceApplication.class, args);
    }

}
