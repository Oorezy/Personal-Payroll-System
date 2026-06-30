package com.introtech.introtechservice.provider;

import com.introtech.introtechservice.common.enums.Currency;

import java.math.BigDecimal;

public record TransferRequest(
        Long paymentId,
        Long workerId,
        String workerName,
        BigDecimal amount,
        Currency currency,
        String idempotencyKey,
        String providerRecipientId,
        String description
) {
}
