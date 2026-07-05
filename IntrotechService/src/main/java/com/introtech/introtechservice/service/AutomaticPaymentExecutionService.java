package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.PaymentStatus;
import com.introtech.introtechservice.entity.PaymentRecord;
import com.introtech.introtechservice.entity.Worker;
import com.introtech.introtechservice.provider.PaymentProviderSelector;
import com.introtech.introtechservice.provider.PaymentProviderService;
import com.introtech.introtechservice.provider.ProviderTransferStatus;
import com.introtech.introtechservice.provider.TransferRequest;
import com.introtech.introtechservice.provider.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AutomaticPaymentExecutionService {

    private final PaymentProviderSelector paymentProviderSelector;

    public void execute(PaymentRecord paymentRecord) {
        Worker worker = paymentRecord.getWorker();

        if (!worker.isPaymentDetailsVerified()) {
            paymentRecord.setStatus(PaymentStatus.FAILED);
            paymentRecord.setFailureReason("Worker payment details are not verified");
            return;
        }

        try {
            PaymentProviderService provider = paymentProviderSelector.selectProvider(
                    paymentRecord.getCurrency()
            );

            TransferResponse response = provider.initiateTransfer(new TransferRequest(
                    paymentRecord.getId(),
                    worker.getId(),
                    worker.getFullName(),
                    paymentRecord.getAmount(),
                    paymentRecord.getCurrency(),
                    paymentRecord.getIdempotencyKey(),
                    worker.getProviderRecipientId(),
                    "Automatic payroll payment to " + worker.getFullName()
            ));

            paymentRecord.setProviderName(response.providerName());
            paymentRecord.setProviderTransactionId(response.providerTransactionId());
            paymentRecord.setProviderTransferReference(response.providerTransferReference());
            paymentRecord.setFailureReason(response.failureReason());
            paymentRecord.setProcessedAt(LocalDateTime.now());
            applyProviderStatus(paymentRecord, response.status());
        } catch (RuntimeException exception) {
            paymentRecord.setStatus(PaymentStatus.FAILED);
            paymentRecord.setFailureReason(exception.getMessage());
        }
    }

    private void applyProviderStatus(
            PaymentRecord paymentRecord,
            ProviderTransferStatus providerStatus
    ) {
        if (providerStatus == ProviderTransferStatus.SUCCESS) {
            paymentRecord.setStatus(PaymentStatus.PAID);
            paymentRecord.setPaidDate(LocalDate.now());
            paymentRecord.setConfirmedAt(LocalDateTime.now());
            return;
        }

        if (providerStatus == ProviderTransferStatus.FAILED) {
            paymentRecord.setStatus(PaymentStatus.FAILED);
            return;
        }

        paymentRecord.setStatus(PaymentStatus.PROCESSING);
    }
}
