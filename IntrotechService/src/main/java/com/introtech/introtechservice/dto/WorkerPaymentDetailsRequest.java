package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkerPaymentDetailsRequest(

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotBlank(message = "Payment region is required")
        String paymentRegion,

        @NotBlank(message = "Account holder name is required")
        String accountHolderName,

        String bankName,

        String bankAccountNumber,

        String iban
) {
}