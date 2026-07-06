package com.introtech.introtechservice.controller;

import com.introtech.introtechservice.common.BaseController;
import com.introtech.introtechservice.common.enums.PaymentStatus;
import com.introtech.introtechservice.dto.MockPaymentFailureRequest;
import com.introtech.introtechservice.dto.NoteRequest;
import com.introtech.introtechservice.dto.PaymentRecordResponse;
import com.introtech.introtechservice.entity.PaymentRecord;
import com.introtech.introtechservice.exceptions.DataValidationException;
import com.introtech.introtechservice.service.PaymentApprovalService;
import com.introtech.introtechservice.service.PaymentConfirmationService;
import com.introtech.introtechservice.service.PaymentRecordGenerationService;
import com.introtech.introtechservice.service.PaymentRecordService;
import com.introtech.introtechservice.service.PaymentRetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentRecordController extends BaseController<PaymentRecord, Long> {

    private final PaymentRecordService paymentRecordService;
    private final PaymentRecordGenerationService paymentRecordGenerationService;
    private final PaymentApprovalService paymentApprovalService;
    private final PaymentConfirmationService paymentConfirmationService;
    private final PaymentRetryService paymentRetryService;

    @GetMapping
    public List<PaymentRecordResponse> getMyPayments(
            @RequestParam(required = false) PaymentStatus status
    ) {
        return paymentRecordService.getMyPayments(status);
    }

    @GetMapping("/{paymentId}")
    public PaymentRecordResponse getPayment(
            @PathVariable Long paymentId
    ) {
        return paymentRecordService.getPayment(paymentId);
    }

    @PostMapping("/{paymentId}/approve")
    public PaymentRecordResponse approvePayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody(required = false) NoteRequest request
    ) throws DataValidationException {
        return paymentApprovalService.approvePayment(paymentId, request);
    }

    @PostMapping("/{paymentId}/retry")
    public PaymentRecordResponse retryPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody(required = false) NoteRequest request
    ) throws DataValidationException {
        return paymentRetryService.retryPayment(paymentId, request);
    }

    @PostMapping("/{paymentId}/cancel")
    public PaymentRecordResponse cancelPayment(@PathVariable Long paymentId) {
        return paymentRecordService.cancelPayment(paymentId);
    }

    @PostMapping("/{paymentId}/skip")
    public PaymentRecordResponse skipPayment(@PathVariable Long paymentId) {
        return paymentRecordService.skipPayment(paymentId);
    }

    @PostMapping("/generate-due")
    public Map<String, Object> generateDuePayments() {
        int generatedCount = paymentRecordGenerationService.generateDuePaymentRecords();

        return Map.of(
                "message", "Due payment generation completed",
                "generatedCount", generatedCount
        );
    }

    @PostMapping("/{paymentId}/mock-confirm-success")
    public PaymentRecordResponse mockConfirmSuccess(
            @PathVariable Long paymentId
    ) {
        return paymentConfirmationService.confirmPaymentSuccess(paymentId);
    }

    @PostMapping("/{paymentId}/mock-confirm-failed")
    public PaymentRecordResponse mockConfirmFailed(
            @PathVariable Long paymentId,
            @RequestBody(required = false) MockPaymentFailureRequest request
    ) {
        return paymentConfirmationService.confirmPaymentFailed(paymentId, request);
    }
}
