package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.PaymentFrequency;
import com.introtech.introtechservice.common.enums.PaymentMode;
import com.introtech.introtechservice.common.enums.ScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentScheduleResponse(
        Long id,
        Long workerId,
        String workerName,
        String scheduleName,
        BigDecimal amount,
        Currency currency,
        PaymentFrequency frequency,
        PaymentMode paymentMode,
        LocalDate startDate,
        LocalDate nextDueDate,
        LocalDate endDate,
        Integer reminderDaysBefore,
        ScheduleStatus status,
        java.time.Instant createdAt,
        java.time.Instant updatedAt
) {
}
