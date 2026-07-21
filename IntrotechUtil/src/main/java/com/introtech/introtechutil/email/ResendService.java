package com.introtech.introtechutil.email;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class ResendService implements EmailService {

    private final Resend resend;
    private final TemplateEngine templateEngine;

    @Value("${resend.email}")
    private String fromEmail;

    public ResendService(@Value("${resend.api-key}") String apiKey, TemplateEngine templateEngine) {
        this.resend = new Resend(apiKey);
        this.templateEngine = templateEngine;
    }

    @Async
    @Override
    public void sendEmail(EmailNotificationVO dto) {
        try {
            log.info("Sending {} email to {} ", dto.getTemplateId(), dto.getEmail());

            Context context = new Context(Locale.getDefault(), dto.getVariables());

            String body = templateEngine.process(dto.getTemplateId(), context);

            CreateEmailOptions request = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(dto.getEmail())
                    .subject(dto.getSubject())
                    .html(body)
                    .build();

            resend.emails().send(request);
        } catch (ResendException e) {
            System.err.println("Failed to send html email: " + e.getMessage());
        }
    }
}
