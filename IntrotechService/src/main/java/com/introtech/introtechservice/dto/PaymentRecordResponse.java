package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentRecordResponse(
        Long id,
        Long workerId,
        String workerName,
        Long scheduleId,
        String scheduleName,
        BigDecimal amount,
        Currency currency,
        LocalDate dueDate,
        LocalDate paidDate,
        PaymentStatus status,
        String paymentMethod,
        String providerName,
        String providerTransferReference,
        String failureReason,
        LocalDateTime approvedAt,
        LocalDateTime processedAt,
        LocalDateTime confirmedAt,
        String notes,
        int retryCount,
        Instant createdDate,
        Instant lastModifiedDate) {
}
