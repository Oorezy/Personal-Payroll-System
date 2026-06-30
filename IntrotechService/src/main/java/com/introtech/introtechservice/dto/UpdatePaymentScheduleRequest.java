package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.PaymentFrequency;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePaymentScheduleRequest(

        String scheduleName,

        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        PaymentFrequency frequency,

        LocalDate nextDueDate,

        LocalDate endDate,

        Integer reminderDaysBefore
) {
}
