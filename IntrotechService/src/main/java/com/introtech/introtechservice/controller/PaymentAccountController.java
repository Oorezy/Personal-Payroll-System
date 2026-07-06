package com.introtech.introtechservice.controller;

import com.introtech.introtechservice.dto.CreatePaymentAccountRequest;
import com.introtech.introtechservice.dto.PaymentAccountResponse;
import com.introtech.introtechservice.exceptions.DataValidationException;
import com.introtech.introtechservice.exceptions.EntityNotFoundException;
import com.introtech.introtechservice.service.PaymentAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-accounts")
@RequiredArgsConstructor
public class PaymentAccountController {

    private final PaymentAccountService paymentAccountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentAccountResponse createPaymentAccount(
            @Valid @RequestBody CreatePaymentAccountRequest request) throws DataValidationException {
        return paymentAccountService.createPaymentAccount(request);
    }

    @GetMapping
    public List<PaymentAccountResponse> getMyPaymentAccounts() {
        return paymentAccountService.getMyPaymentAccounts();
    }

    @GetMapping("/{accountId}")
    public PaymentAccountResponse getPaymentAccount(
            @PathVariable Long accountId) throws EntityNotFoundException {
        return paymentAccountService.getPaymentAccount(accountId);
    }

    @PatchMapping("/{accountId}/set-default")
    public PaymentAccountResponse setDefaultAccount(
            @PathVariable Long accountId) throws EntityNotFoundException {
        return paymentAccountService.setDefaultAccount(accountId);
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePaymentAccount(
            @PathVariable Long accountId) throws EntityNotFoundException {
        paymentAccountService.removePaymentAccount(accountId);
    }
}