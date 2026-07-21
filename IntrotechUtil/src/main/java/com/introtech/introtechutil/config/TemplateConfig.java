package com.introtech.introtechutil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

@Configuration
@EnableAsync
public class TemplateConfig {

    @Value("${HOME_DIR}")
    private String dir;

    @Bean
    public FileTemplateResolver fileTemplateResolver() {

        FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setTemplateMode(TemplateMode.HTML);
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setPrefix(dir + "/emailtemplates/");
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setOrder(1);
        fileTemplateResolver.setCheckExistence(true);

        return fileTemplateResolver;
    }

    @Bean
    public ClassLoaderTemplateResolver classLoaderTemplateResolver() {

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);
        templateResolver.setPrefix("emailtemplates/");
        templateResolver.setSuffix(".html");
        templateResolver.setOrder(2);
        templateResolver.setCheckExistence(true);

        return templateResolver;
    }
}
