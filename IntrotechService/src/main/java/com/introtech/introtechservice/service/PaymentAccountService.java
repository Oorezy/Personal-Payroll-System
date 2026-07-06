package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.PaymentAccountStatus;
import com.introtech.introtechservice.dto.CreatePaymentAccountRequest;
import com.introtech.introtechservice.dto.PaymentAccountResponse;
import com.introtech.introtechservice.entity.PaymentAccount;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.exceptions.DataValidationException;
import com.introtech.introtechservice.exceptions.EntityNotFoundException;
import com.introtech.introtechservice.mappers.PaymentAccountMapper;
import com.introtech.introtechservice.repository.PaymentAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentAccountService {

    private final PaymentAccountRepository paymentAccountRepository;
    private final UserContextService userContextService;
    private final PaymentAccountMapper paymentAccountMapper;

    @Transactional
    public PaymentAccountResponse createPaymentAccount(CreatePaymentAccountRequest request) throws DataValidationException {
        User currentUser = userContextService.getCurrentUser();

        validatePaymentAccount(request);

        boolean shouldBeDefault = Boolean.TRUE.equals(request.defaultAccount())
                || !paymentAccountRepository.existsByOwnerIdAndCurrencyAndStatus(
                currentUser.getId(),
                request.currency(),
                PaymentAccountStatus.ACTIVE
        );

        if (shouldBeDefault) {
            paymentAccountRepository.clearDefaultForCurrency(
                    currentUser.getId(),
                    request.currency()
            );
        }

        PaymentAccount account = PaymentAccount.builder()
                .ownerId(currentUser.getId())
                .currency(request.currency())
                .paymentRegion(normalize(request.paymentRegion()))
                .providerName(resolveProviderName(request.providerName()))
                .accountHolderName(normalize(request.accountHolderName()))
                .bankName(normalize(request.bankName()))
                .defaultAccount(shouldBeDefault)
                .status(PaymentAccountStatus.ACTIVE)
                .build();

        if (request.currency() == Currency.NGN) {
            account.setMaskedAccountNumber(maskBankAccountNumber(request.bankAccountNumber()));
            account.setMaskedIban(null);
        }

        if (request.currency() == Currency.EUR) {
            account.setMaskedIban(maskIban(request.iban()));
            account.setMaskedAccountNumber(null);
        }

        /*
         * For the mock provider, verified = true after local validation.
         * Later, for real providers, this should only become true after
         * provider authorization or account verification succeeds.
         */
        account.setVerified(true);
        account.setProviderCustomerId("mock_customer_" + currentUser.getId());
        account.setProviderAccountId("mock_account_" + Random.from(new Random()).nextLong());
        account.setProviderAuthorizationId("mock_auth_" + Random.from(new Random()).nextLong());

        PaymentAccount savedAccount = paymentAccountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<PaymentAccountResponse> getMyPaymentAccounts() {
        User currentUser = userContextService.getCurrentUser();

        return paymentAccountRepository
                .findByOwnerIdAndStatusOrderByCreatedDateDesc(
                        currentUser.getId(),
                        PaymentAccountStatus.ACTIVE
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentAccountResponse getPaymentAccount(Long accountId) throws EntityNotFoundException {
        User currentUser = userContextService.getCurrentUser();

        PaymentAccount account = getOwnedPaymentAccount(accountId, currentUser.getId());

        return mapToResponse(account);
    }

    @Transactional
    public PaymentAccountResponse setDefaultAccount(Long accountId) throws EntityNotFoundException {
        User currentUser = userContextService.getCurrentUser();

        PaymentAccount account = getOwnedPaymentAccount(accountId, currentUser.getId());

        if (account.getStatus() != PaymentAccountStatus.ACTIVE) {
            throw new ResponseStatusException(BAD_REQUEST, "Only active accounts can be set as default");
        }

        paymentAccountRepository.clearDefaultForCurrency(
                currentUser.getId(),
                account.getCurrency()
        );

        account.setDefaultAccount(true);

        PaymentAccount savedAccount = paymentAccountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    @Transactional
    public void removePaymentAccount(Long accountId) throws EntityNotFoundException {
        User currentUser = userContextService.getCurrentUser();

        PaymentAccount account = getOwnedPaymentAccount(accountId, currentUser.getId());

        account.setStatus(PaymentAccountStatus.REMOVED);
        account.setDefaultAccount(false);

        paymentAccountRepository.save(account);
    }

    public PaymentAccount getDefaultPaymentAccount(Long userId, Currency currency) throws DataValidationException {
        PaymentAccount account = paymentAccountRepository
                .findByOwnerIdAndCurrencyAndDefaultAccountTrueAndStatus(
                        userId,
                        currency,
                        PaymentAccountStatus.ACTIVE
                )
                .orElseThrow(() -> new DataValidationException(
                        "No active default payment account found for currency: " + currency));

        if (!account.isVerified()) {
            throw new DataValidationException("Default payment account is not verified");
        }

        return account;
    }

    private PaymentAccount getOwnedPaymentAccount(Long accountId, Long userId) throws EntityNotFoundException {
        return paymentAccountRepository.findByIdAndOwnerId(accountId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Payment account"));
    }

    private void validatePaymentAccount(CreatePaymentAccountRequest request) throws DataValidationException {
        if (request.currency() == Currency.NGN) {
            validateNgnAccount(request);
            return;
        }

        if (request.currency() == Currency.EUR) {
            validateEurAccount(request);
            return;
        }

        throw new ResponseStatusException(BAD_REQUEST, "Unsupported currency");
    }

    private void validateNgnAccount(CreatePaymentAccountRequest request) throws DataValidationException {
        if (isBlank(request.bankName())) {
            throw new DataValidationException("Bank name is required for NGN payment account");
        }

        if (isBlank(request.bankAccountNumber())) {
            throw new ResponseStatusException(BAD_REQUEST, "Bank account number is required for NGN payment account");
        }

        String cleaned = request.bankAccountNumber().replaceAll("\\s+", "");

        if (!cleaned.matches("\\d{10}")) {
            throw new ResponseStatusException(BAD_REQUEST, "Nigerian account number must be 10 digits");
        }
    }

    private void validateEurAccount(CreatePaymentAccountRequest request) {
        if (isBlank(request.iban())) {
            throw new ResponseStatusException(BAD_REQUEST, "IBAN is required for EUR payment account");
        }

        String cleanedIban = request.iban().replaceAll("\\s+", "").toUpperCase();

        if (!cleanedIban.matches("[A-Z]{2}[0-9A-Z]{13,32}")) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid IBAN format");
        }
    }

    private PaymentAccountResponse mapToResponse(PaymentAccount account) {
        return paymentAccountMapper.toDto(account);
//        return new PaymentAccountResponse(
//                account.getId(),
//                account.getCurrency(),
//                account.getPaymentRegion(),
//                account.getProviderName(),
//                account.getAccountHolderName(),
//                account.getBankName(),
//                account.getMaskedAccountNumber(),
//                account.getMaskedIban(),
//                account.isVerified(),
//                account.isDefaultAccount(),
//                account.getStatus(),
//                account.getCreatedAt(),
//                account.getUpdatedAt()
//        );
    }

    private String resolveProviderName(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return "MOCK_PROVIDER";
        }

        return providerName.trim();
    }

    private String maskBankAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }

        String cleaned = accountNumber.replaceAll("\\s+", "");

        if (cleaned.length() <= 4) {
            return "****";
        }

        return "******" + cleaned.substring(cleaned.length() - 4);
    }

    private String maskIban(String iban) {
        if (iban == null) {
            return null;
        }

        String cleaned = iban.replaceAll("\\s+", "").toUpperCase();

        if (cleaned.length() <= 8) {
            return "********";
        }

        String firstFour = cleaned.substring(0, 4);
        String lastFour = cleaned.substring(cleaned.length() - 4);

        return firstFour + "********" + lastFour;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
