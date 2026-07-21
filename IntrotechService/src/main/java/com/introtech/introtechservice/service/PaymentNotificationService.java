package com.introtech.introtechservice.service;

import com.introtech.introtechservice.entity.PaymentRecord;
import com.introtech.introtechutil.email.EmailNotificationVO;
import com.introtech.introtechutil.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentNotificationService {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final EmailService emailService;

    public void sendPendingApprovalEmail(PaymentRecord paymentRecord) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", fullName(paymentRecord));
        variables.put("workerName", paymentRecord.getWorker().getFullName());
        variables.put("scheduleName", paymentRecord.getSchedule() == null ? "One-off payment" : paymentRecord.getSchedule().getScheduleName());
        variables.put("amount", formatAmount(paymentRecord));
        variables.put("dueDate", paymentRecord.getDueDate().format(DISPLAY_DATE));
        variables.put("paymentId", paymentRecord.getId());

        emailService.sendEmail(EmailNotificationVO.builder()
                .email(paymentRecord.getUser().getEmail())
                .subject("Payment awaiting approval")
                .templateId("pending-payment-approval")
                .variables(variables)
                .build());
    }

    private String fullName(PaymentRecord paymentRecord) {
        String firstName = paymentRecord.getUser().getFirstName() == null ? "" : paymentRecord.getUser().getFirstName();
        String lastName = paymentRecord.getUser().getLastName() == null ? "" : paymentRecord.getUser().getLastName();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? "there" : fullName;
    }

    private String formatAmount(PaymentRecord paymentRecord) {
        Locale locale = paymentRecord.getCurrency().name().equals("NGN")
                ? Locale.forLanguageTag("en-NG")
                : Locale.GERMANY;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(java.util.Currency.getInstance(paymentRecord.getCurrency().name()));
        return formatter.format(paymentRecord.getAmount());
    }
}
