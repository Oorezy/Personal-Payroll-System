package com.introtech.introtechservice.controller;

import com.introtech.introtechservice.dto.WorkerPaymentDetailsRequest;
import com.introtech.introtechservice.dto.WorkerPaymentDetailsResponse;
import com.introtech.introtechservice.service.WorkerPaymentDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workers/{workerId}/payment-details")
@RequiredArgsConstructor
public class WorkerPaymentDetailsController {

    private final WorkerPaymentDetailsService workerPaymentDetailsService;

    @PostMapping
    public WorkerPaymentDetailsResponse addPaymentDetails(
            @PathVariable Long workerId,
            @Valid @RequestBody WorkerPaymentDetailsRequest request
    ) {
        return workerPaymentDetailsService.savePaymentDetails(workerId, request);
    }

    @PutMapping
    public WorkerPaymentDetailsResponse updatePaymentDetails(
            @PathVariable Long workerId,
            @Valid @RequestBody WorkerPaymentDetailsRequest request
    ) {
        return workerPaymentDetailsService.savePaymentDetails(workerId, request);
    }

    @GetMapping
    public WorkerPaymentDetailsResponse getPaymentDetails(
            @PathVariable Long workerId
    ) {
        return workerPaymentDetailsService.getPaymentDetails(workerId);
    }
}
