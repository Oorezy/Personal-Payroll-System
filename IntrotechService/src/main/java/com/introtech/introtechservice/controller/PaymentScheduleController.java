package com.introtech.introtechservice.controller;

import com.introtech.introtechservice.common.BaseController;
import com.introtech.introtechservice.dto.CreatePaymentScheduleRequest;
import com.introtech.introtechservice.dto.PaymentScheduleResponse;
import com.introtech.introtechservice.dto.UpdatePaymentScheduleRequest;
import com.introtech.introtechservice.entity.PaymentSchedule;
import com.introtech.introtechservice.service.PaymentScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payment-schedules")
@RequiredArgsConstructor
public class PaymentScheduleController extends BaseController<PaymentSchedule, Long> {

    private final PaymentScheduleService paymentScheduleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentScheduleResponse createSchedule(
            @Valid @RequestBody CreatePaymentScheduleRequest request
    ) {
        return paymentScheduleService.createSchedule(request);
    }

    @GetMapping
    public List<PaymentScheduleResponse> getMySchedules() {
        return paymentScheduleService.getMySchedules();
    }

    @GetMapping("/{scheduleId}")
    public PaymentScheduleResponse getSchedule(
            @PathVariable Long scheduleId
    ) {
        return paymentScheduleService.getSchedule(scheduleId);
    }

    @PutMapping("/{scheduleId}")
    public PaymentScheduleResponse updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdatePaymentScheduleRequest request
    ) {
        return paymentScheduleService.updateSchedule(scheduleId, request);
    }

    @PatchMapping("/{scheduleId}/pause")
    public PaymentScheduleResponse pauseSchedule(
            @PathVariable Long scheduleId
    ) {
        return paymentScheduleService.pauseSchedule(scheduleId);
    }

    @PatchMapping("/{scheduleId}/resume")
    public PaymentScheduleResponse resumeSchedule(
            @PathVariable Long scheduleId
    ) {
        return paymentScheduleService.resumeSchedule(scheduleId);
    }

    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSchedule(
            @PathVariable Long scheduleId
    ) {
        paymentScheduleService.cancelSchedule(scheduleId);
    }
}
