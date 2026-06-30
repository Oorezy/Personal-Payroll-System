package com.introtech.introtechservice.provider;


public record TransferResponse(
        String providerName,
        String providerTransactionId,
        String providerTransferReference,
        ProviderTransferStatus status,
        String failureReason
) {
}
