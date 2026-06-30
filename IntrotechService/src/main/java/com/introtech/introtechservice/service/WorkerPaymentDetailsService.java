package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.dto.WorkerPaymentDetailsRequest;
import com.introtech.introtechservice.dto.WorkerPaymentDetailsResponse;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.entity.Worker;
import com.introtech.introtechservice.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class WorkerPaymentDetailsService {

    private final WorkerRepository workerRepository;
    private final UserContextService userContextService;

    @Transactional
    public WorkerPaymentDetailsResponse savePaymentDetails(
            Long workerId,
            WorkerPaymentDetailsRequest request
    ) {
        User currentUser = userContextService.getCurrentUser();

        Worker worker = workerRepository.findByIdAndUserId(workerId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Worker not found"));

        validatePaymentDetails(request);

        worker.setCurrency(request.currency());
        worker.setPaymentRegion(normalize(request.paymentRegion()));
        worker.setAccountHolderName(normalize(request.accountHolderName()));
        worker.setBankName(normalize(request.bankName()));

        if (request.currency() == Currency.NGN) {
            worker.setBankAccountNumberMasked(maskBankAccountNumber(request.bankAccountNumber()));
            worker.setIbanMasked(null);
        }

        if (request.currency() == Currency.EUR) {
            worker.setIbanMasked(maskIban(request.iban()));
            worker.setBankAccountNumberMasked(null);
        }

        /*
         * For now, this means "basic app validation passed".
         * Later, when we integrate Paystack/Flutterwave/Stripe/Wise,
         * this should only become true after provider-side recipient verification.
         */
        worker.setPaymentDetailsVerified(true);

        Worker savedWorker = workerRepository.save(worker);

        return mapToResponse(savedWorker);
    }

    @Transactional(readOnly = true)
    public WorkerPaymentDetailsResponse getPaymentDetails(Long workerId) {
        User currentUser = userContextService.getCurrentUser();

        Worker worker = workerRepository.findByIdAndUserId(workerId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Worker not found"));

        return mapToResponse(worker);
    }

    private void validatePaymentDetails(WorkerPaymentDetailsRequest request) {
        if (request.currency() == Currency.NGN) {
            validateNgnPaymentDetails(request);
            return;
        }

        if (request.currency() == Currency.EUR) {
            validateEurPaymentDetails(request);
            return;
        }

        throw new ResponseStatusException(BAD_REQUEST, "Unsupported currency");
    }

    private void validateNgnPaymentDetails(WorkerPaymentDetailsRequest request) {
        if (isBlank(request.bankName())) {
            throw new ResponseStatusException(BAD_REQUEST, "Bank name is required for NGN payments");
        }

        if (isBlank(request.bankAccountNumber())) {
            throw new ResponseStatusException(BAD_REQUEST, "Bank account number is required for NGN payments");
        }

        String cleanedAccountNumber = request.bankAccountNumber().replaceAll("\\s+", "");

        if (!cleanedAccountNumber.matches("\\d{10}")) {
            throw new ResponseStatusException(BAD_REQUEST, "Nigerian account number must be 10 digits");
        }
    }

    private void validateEurPaymentDetails(WorkerPaymentDetailsRequest request) {
        if (isBlank(request.iban())) {
            throw new ResponseStatusException(BAD_REQUEST, "IBAN is required for EUR payments");
        }

        String cleanedIban = request.iban().replaceAll("\\s+", "").toUpperCase();

        /*
         * Simple IBAN format validation.
         * This is not full IBAN checksum validation yet.
         * We can add full checksum validation later.
         */
        if (!cleanedIban.matches("[A-Z]{2}[0-9A-Z]{13,32}")) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid IBAN format");
        }
    }

    private WorkerPaymentDetailsResponse mapToResponse(Worker worker) {
        return new WorkerPaymentDetailsResponse(
                worker.getId(),
                worker.getFullName(),
                worker.getCurrency(),
                worker.getPaymentRegion(),
                worker.getAccountHolderName(),
                worker.getBankName(),
                worker.getBankAccountNumberMasked(),
                worker.getIbanMasked(),
                worker.isPaymentDetailsVerified(),
                worker.getProviderRecipientId()
        );
    }

    private String maskBankAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }

        String cleaned = accountNumber.replaceAll("\\s+", "");

        if (cleaned.length() <= 4) {
            return "****";
        }

        String lastFourDigits = cleaned.substring(cleaned.length() - 4);

        return "******" + lastFourDigits;
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
