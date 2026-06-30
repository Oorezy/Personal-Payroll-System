package com.introtech.introtechservice.provider;


import com.introtech.introtechservice.common.enums.Currency;

public interface PaymentProviderService {

    String providerName();

    boolean supports(Currency currency);

    TransferResponse initiateTransfer(TransferRequest request);
}
