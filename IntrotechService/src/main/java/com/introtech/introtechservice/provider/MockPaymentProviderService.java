package com.introtech.introtechservice.provider;


import com.introtech.introtechservice.common.enums.Currency;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MockPaymentProviderService implements PaymentProviderService {

    @Override
    public String providerName() {
        return "MOCK_PROVIDER";
    }

    @Override
    public boolean supports(Currency currency) {
        return currency == Currency.EUR || currency == Currency.NGN;
    }

    @Override
    public TransferResponse initiateTransfer(TransferRequest request) {
        String transactionId = "mock_txn_" + UUID.randomUUID();
        String transferReference = "mock_ref_" + request.idempotencyKey();

        return new TransferResponse(
                providerName(),
                transactionId,
                transferReference,
                ProviderTransferStatus.PROCESSING,
                null
        );
    }
}
