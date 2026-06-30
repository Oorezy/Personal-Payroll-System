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
public class PaymentApprovalService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final UserContextService userContextService;
    private final PaymentProviderSelector paymentProviderSelector;
    private final PaymentRecordMapper paymentRecordMapper;

    @Transactional
    public PaymentRecordResponse approvePayment(
            Long paymentId,
            NoteRequest request
    ) {
        User currentUser = userContextService.getCurrentUser();

        PaymentRecord paymentRecord = paymentRecordRepository
                .findByIdAndUserId(paymentId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Payment not found"));

        validatePaymentCanBeApproved(paymentRecord);

        Worker worker = paymentRecord.getWorker();

        PaymentProviderService provider = paymentProviderSelector.selectProvider(
                paymentRecord.getCurrency()
        );

        String idempotencyKey = ensureIdempotencyKey(paymentRecord);

        TransferRequest transferRequest = new TransferRequest(
                paymentRecord.getId(),
                worker.getId(),
                worker.getFullName(),
                paymentRecord.getAmount(),
                paymentRecord.getCurrency(),
                idempotencyKey,
                worker.getProviderRecipientId(),
                buildPaymentDescription(paymentRecord)
        );

        TransferResponse transferResponse = provider.initiateTransfer(transferRequest);

        paymentRecord.setApprovedAt(LocalDateTime.now());
        paymentRecord.setProcessedAt(LocalDateTime.now());
        paymentRecord.setProviderName(transferResponse.providerName());
        paymentRecord.setProviderTransactionId(transferResponse.providerTransactionId());
        paymentRecord.setProviderTransferReference(transferResponse.providerTransferReference());
        paymentRecord.setFailureReason(transferResponse.failureReason());

        if (request != null && request.note() != null && !request.note().isBlank()) {
            paymentRecord.setNotes(request.note().trim());
        }

        applyProviderStatus(paymentRecord, transferResponse.status());

        PaymentRecord savedPayment = paymentRecordRepository.save(paymentRecord);

        return paymentRecordMapper.toDto(savedPayment);
    }

    private void validatePaymentCanBeApproved(PaymentRecord paymentRecord) {
        if (paymentRecord.getStatus() != PaymentStatus.AWAITING_APPROVAL) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Only payments awaiting approval can be approved"
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

        if (paymentRecord.getDueDate() == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Payment due date is missing"
            );
        }
    }

    private String ensureIdempotencyKey(PaymentRecord paymentRecord) {
        if (paymentRecord.getIdempotencyKey() != null && !paymentRecord.getIdempotencyKey().isBlank()) {
            return paymentRecord.getIdempotencyKey();
        }

        String idempotencyKey = "payment-" + paymentRecord.getId() + "-" + LocalDate.now();

        paymentRecord.setIdempotencyKey(idempotencyKey);

        return idempotencyKey;
    }

    private String buildPaymentDescription(PaymentRecord paymentRecord) {
        return "Payroll payment to "
                + paymentRecord.getWorker().getFullName()
                + " for due date "
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
