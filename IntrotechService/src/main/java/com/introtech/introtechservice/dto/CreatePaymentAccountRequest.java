package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentAccountRequest(

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotBlank(message = "Payment region is required")
        String paymentRegion,

        String providerName,

        @NotBlank(message = "Account holder name is required")
        String accountHolderName,

        String bankName,

        String bankAccountNumber,

        String iban,

        Boolean defaultAccount
) {
}
