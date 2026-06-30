package com.introtech.introtechservice.provider;


import com.introtech.introtechservice.common.enums.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@RequiredArgsConstructor
public class PaymentProviderSelector {

    private final List<PaymentProviderService> paymentProviders;

    public PaymentProviderService selectProvider(Currency currency) {
        return paymentProviders.stream()
                .filter(provider -> provider.supports(currency))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "No payment provider available for currency: " + currency
                ));
    }
}
