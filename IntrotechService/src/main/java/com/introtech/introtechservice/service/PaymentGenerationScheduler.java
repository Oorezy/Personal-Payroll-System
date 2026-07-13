package com.introtech.introtechservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGenerationScheduler {

    private final PaymentRecordGenerationService paymentRecordGenerationService;

    /**
     * Runs every day at 6:00 AM server time.
     * For local development, you can temporarily change this to fixedRate.
     */
//    @Scheduled(cron = "0 0 6 * * *")
    @Scheduled(fixedRate = 150000)
    public void generateDuePaymentsDaily() {
        int generatedCount = paymentRecordGenerationService.generateDuePaymentRecords();

        log.info("Payment generation job completed. Generated {} payment records.", generatedCount);
    }
}
