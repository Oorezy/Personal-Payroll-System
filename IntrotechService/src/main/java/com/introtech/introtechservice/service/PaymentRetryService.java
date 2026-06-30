package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.PaymentStatus;
import com.introtech.introtechservice.dto.NoteRequest;
import com.introtech.introtechservice.dto.PaymentRecordResponse;
import com.introtech.introtechservice.entity.PaymentRecord;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.entity.Worker;
import com.introtech.introtechservice.mappers.PaymentRecordMapper;
import com.introtech.introtechservice.provider.PaymentProviderSelector;
import com.introtech.introtechservice.provider.PaymentProviderService;
import com.introtech.introtechservice.provider.ProviderTransferStatus;
import com.introtech.introtechservice.provider.TransferRequest;
import com.introtech.introtechservice.provider.TransferResponse;
import com.introtech.introtechservice.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentRetryService {

    private static final int MAX_RETRY_COUNT = 3;

    private final PaymentRecordRepository paymentRecordRepository;
    private final UserContextService userContextService;
    private final PaymentProviderSelector paymentProviderSelector;
    private final PaymentRecordMapper paymentRecordMapper;

    @Transactional
    public PaymentRecordResponse retryPayment(
            Long paymentId,
            NoteRequest request
    ) {
        User currentUser = userContextService.getCurrentUser();

        PaymentRecord paymentRecord = paymentRecordRepository
                .findByIdAndUserId(paymentId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Payment not found"));

        validatePaymentCanBeRetried(paymentRecord);

        Worker worker = paymentRecord.getWorker();

        PaymentProviderService provider = paymentProviderSelector.selectProvider(
                paymentRecord.getCurrency()
        );

        int nextRetryCount = paymentRecord.getRetryCount() + 1;

        String retryIdempotencyKey = buildRetryIdempotencyKey(paymentRecord, nextRetryCount);

        TransferRequest transferRequest = new TransferRequest(
                paymentRecord.getId(),
                worker.getId(),
                worker.getFullName(),
                paymentRecord.getAmount(),
                paymentRecord.getCurrency(),
                retryIdempotencyKey,
                worker.getProviderRecipientId(),
                buildPaymentDescription(paymentRecord, nextRetryCount)
        );

        TransferResponse transferResponse = provider.initiateTransfer(transferRequest);

        paymentRecord.setRetryCount(nextRetryCount);
        paymentRecord.setIdempotencyKey(retryIdempotencyKey);
        paymentRecord.setProviderName(transferResponse.providerName());
        paymentRecord.setProviderTransactionId(transferResponse.providerTransactionId());
        paymentRecord.setProviderTransferReference(transferResponse.providerTransferReference());
        paymentRecord.setFailureReason(transferResponse.failureReason());
        paymentRecord.setProcessedAt(LocalDateTime.now());
        paymentRecord.setConfirmedAt(null);

        if (request != null && request.note() != null && !request.note().isBlank()) {
            paymentRecord.setNotes(request.note().trim());
        }

        applyProviderStatus(paymentRecord, transferResponse.status());

        PaymentRecord savedPayment = paymentRecordRepository.save(paymentRecord);

        return paymentRecordMapper.toDto(savedPayment);
    }

    private void validatePaymentCanBeRetried(PaymentRecord paymentRecord) {
        if (paymentRecord.getStatus() != PaymentStatus.FAILED) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Only failed payments can be retried"
            );
        }

        if (paymentRecord.getRetryCount() >= MAX_RETRY_COUNT) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Maximum retry count reached for this payment"
            );
        }

        Worker worker = paymentRecord.getWorker();

        if (!worker.isPaymentDetailsVerified()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Worker payment details are not verified"
            );
        }

        if (paymentRecord.getAmount() == null || paymentRecord.getAmount().signum() <= 0) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Payment amount must be greater than zero"
            );
        }
    }

    private String buildRetryIdempotencyKey(
            PaymentRecord paymentRecord,
            int retryCount
    ) {
        return "payment-"
                + paymentRecord.getId()
                + "-retry-"
                + retryCount
                + "-"
                + LocalDate.now();
    }

    private String buildPaymentDescription(
            PaymentRecord paymentRecord,
            int retryCount
    ) {
        return "Retry "
                + retryCount
                + " for payroll payment to "
                + paymentRecord.getWorker().getFullName()
                + " due on "
                + paymentRecord.getDueDate();
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
