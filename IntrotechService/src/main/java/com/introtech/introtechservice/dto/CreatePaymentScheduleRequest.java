package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.PaymentFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePaymentScheduleRequest(

        @NotNull(message = "Worker ID is required")
        Long workerId,

        @NotBlank(message = "Schedule name is required")
        String scheduleName,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotNull(message = "Payment frequency is required")
        PaymentFrequency frequency,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "First due date is required")
        LocalDate firstDueDate,

        LocalDate endDate,

        @Min(value = 1, message = "Reminder days before must be at least 1")
        Integer reminderDaysBefore
) {
}
