package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;

public record WorkerPaymentDetailsResponse(
        Long workerId,
        String workerName,
        Currency currency,
        String paymentRegion,
        String accountHolderName,
        String bankName,
        String maskedBankAccountNumber,
        String maskedIban,
        boolean paymentDetailsVerified,
        String providerRecipientId
) {
}
