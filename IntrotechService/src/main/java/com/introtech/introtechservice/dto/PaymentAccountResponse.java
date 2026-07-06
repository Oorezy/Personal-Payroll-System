package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.PaymentAccountStatus;

import java.time.Instant;

public record PaymentAccountResponse(
        Long id,
        Currency currency,
        String paymentRegion,
        String providerName,
        String accountHolderName,
        String bankName,
        String maskedAccountNumber,
        String maskedIban,
        boolean verified,
        boolean defaultAccount,
        PaymentAccountStatus status,
        Instant createdDate,
        Instant lastModifiedDate) {
}
