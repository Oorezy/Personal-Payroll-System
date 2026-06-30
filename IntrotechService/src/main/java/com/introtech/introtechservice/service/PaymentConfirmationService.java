package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.PaymentStatus;
import com.introtech.introtechservice.dto.MockPaymentFailureRequest;
import com.introtech.introtechservice.dto.PaymentRecordResponse;
import com.introtech.introtechservice.entity.PaymentRecord;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.mappers.PaymentRecordMapper;
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
public class PaymentConfirmationService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final UserContextService userContextService;
    private final PaymentRecordMapper paymentRecordMapper;

    @Transactional
    public PaymentRecordResponse confirmPaymentSuccess(Long paymentId) {
        User currentUser = userContextService.getCurrentUser();

        PaymentRecord paymentRecord = paymentRecordRepository
                .findByIdAndUserId(paymentId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Payment not found"));

        validateCanBeConfirmed(paymentRecord);

        paymentRecord.setStatus(PaymentStatus.PAID);
        paymentRecord.setPaidDate(LocalDate.now());
        paymentRecord.setConfirmedAt(LocalDateTime.now());
        paymentRecord.setFailureReason(null);

        PaymentRecord savedPayment = paymentRecordRepository.save(paymentRecord);

        return paymentRecordMapper.toDto(savedPayment);
    }

    @Transactional
    public PaymentRecordResponse confirmPaymentFailed(
            Long paymentId,
            MockPaymentFailureRequest request
    ) {
        User currentUser = userContextService.getCurrentUser();

        PaymentRecord paymentRecord = paymentRecordRepository
                .findByIdAndUserId(paymentId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Payment not found"));

        validateCanBeConfirmed(paymentRecord);

        paymentRecord.setStatus(PaymentStatus.FAILED);
        paymentRecord.setConfirmedAt(LocalDateTime.now());

        String failureReason = request == null || request.failureReason() == null
                ? "Payment failed at provider"
                : request.failureReason().trim();

        paymentRecord.setFailureReason(failureReason);

        PaymentRecord savedPayment = paymentRecordRepository.save(paymentRecord);

        return paymentRecordMapper.toDto(savedPayment);
    }

    private void validateCanBeConfirmed(PaymentRecord paymentRecord) {
        if (paymentRecord.getStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Payment has already been confirmed as paid"
            );
        }

        if (paymentRecord.getStatus() == PaymentStatus.FAILED) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Payment has already been confirmed as failed"
            );
        }

        if (paymentRecord.getStatus() != PaymentStatus.PROCESSING) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Only processing payments can be confirmed"
            );
        }

        if (paymentRecord.getProviderTransferReference() == null
                || paymentRecord.getProviderTransferReference().isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Payment does not have a provider transfer reference"
            );
        }
    }
}
