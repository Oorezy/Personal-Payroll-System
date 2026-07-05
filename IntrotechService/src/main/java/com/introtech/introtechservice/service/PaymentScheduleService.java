package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.PaymentMode;
import com.introtech.introtechservice.common.enums.ScheduleStatus;
import com.introtech.introtechservice.common.enums.WorkerStatus;
import com.introtech.introtechservice.dto.CreatePaymentScheduleRequest;
import com.introtech.introtechservice.dto.PaymentScheduleResponse;
import com.introtech.introtechservice.dto.UpdatePaymentScheduleRequest;
import com.introtech.introtechservice.entity.PaymentSchedule;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.entity.Worker;
import com.introtech.introtechservice.repository.PaymentScheduleRepository;
import com.introtech.introtechservice.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentScheduleService {

    private final PaymentScheduleRepository paymentScheduleRepository;
    private final WorkerRepository workerRepository;
    private final UserContextService userContextService;

    @Transactional
    public PaymentScheduleResponse createSchedule(CreatePaymentScheduleRequest request) {
        User currentUser = userContextService.getCurrentUser();

        Worker worker = workerRepository.findByIdAndUserId(request.workerId(), currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Worker not found"));

        validateWorkerForSchedule(worker);
        validateCreateRequest(request, worker);

        PaymentSchedule schedule = PaymentSchedule.builder()
                .user(currentUser)
                .worker(worker)
                .scheduleName(request.scheduleName().trim())
                .amount(request.amount())
                .currency(request.currency())
                .frequency(request.frequency())
                .paymentMode(request.paymentMode() == null
                        ? PaymentMode.MANUAL_APPROVAL
                        : request.paymentMode())
                .startDate(request.startDate())
                .nextDueDate(request.firstDueDate())
                .endDate(request.endDate())
                .reminderDaysBefore(
                        request.reminderDaysBefore() == null
                                ? 1
                                : request.reminderDaysBefore()
                )
                .status(ScheduleStatus.ACTIVE)
                .build();

        PaymentSchedule savedSchedule = paymentScheduleRepository.save(schedule);

        return mapToResponse(savedSchedule);
    }

    @Transactional(readOnly = true)
    public List<PaymentScheduleResponse> getMySchedules() {
        User currentUser = userContextService.getCurrentUser();

        return paymentScheduleRepository
                .findByUserIdAndStatusNotOrderByCreatedDateDesc(
                        currentUser.getId(),
                        ScheduleStatus.CANCELLED
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentScheduleResponse getSchedule(Long scheduleId) {
        User currentUser = userContextService.getCurrentUser();

        PaymentSchedule schedule = getScheduleOwnedByCurrentUser(
                scheduleId,
                currentUser.getId()
        );

        return mapToResponse(schedule);
    }

    @Transactional
    public PaymentScheduleResponse updateSchedule(
            Long scheduleId,
            UpdatePaymentScheduleRequest request
    ) {
        User currentUser = userContextService.getCurrentUser();

        PaymentSchedule schedule = getScheduleOwnedByCurrentUser(
                scheduleId,
                currentUser.getId()
        );

        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot update a cancelled schedule");
        }

        if (request.scheduleName() != null && !request.scheduleName().isBlank()) {
            schedule.setScheduleName(request.scheduleName().trim());
        }

        if (request.amount() != null) {
            validateAmount(request.amount());
            schedule.setAmount(request.amount());
        }

        if (request.frequency() != null) {
            schedule.setFrequency(request.frequency());
        }

        if (request.paymentMode() != null) {
            schedule.setPaymentMode(request.paymentMode());
        }

        if (request.nextDueDate() != null) {
            validateNextDueDate(request.nextDueDate(), schedule.getStartDate());
            schedule.setNextDueDate(request.nextDueDate());
        }

        if (request.endDate() != null) {
            validateEndDate(request.endDate(), schedule.getNextDueDate());
            schedule.setEndDate(request.endDate());
        }

        if (request.reminderDaysBefore() != null) {
            validateReminderDays(request.reminderDaysBefore());
            schedule.setReminderDaysBefore(request.reminderDaysBefore());
        }

        PaymentSchedule updatedSchedule = paymentScheduleRepository.save(schedule);

        return mapToResponse(updatedSchedule);
    }

    @Transactional
    public PaymentScheduleResponse pauseSchedule(Long scheduleId) {
        User currentUser = userContextService.getCurrentUser();

        PaymentSchedule schedule = getScheduleOwnedByCurrentUser(
                scheduleId,
                currentUser.getId()
        );

        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot pause a cancelled schedule");
        }

        schedule.setStatus(ScheduleStatus.PAUSED);

        return mapToResponse(paymentScheduleRepository.save(schedule));
    }

    @Transactional
    public PaymentScheduleResponse resumeSchedule(Long scheduleId) {
        User currentUser = userContextService.getCurrentUser();

        PaymentSchedule schedule = getScheduleOwnedByCurrentUser(
                scheduleId,
                currentUser.getId()
        );

        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot resume a cancelled schedule");
        }

        schedule.setStatus(ScheduleStatus.ACTIVE);

        return mapToResponse(paymentScheduleRepository.save(schedule));
    }

    @Transactional
    public void cancelSchedule(Long scheduleId) {
        User currentUser = userContextService.getCurrentUser();

        PaymentSchedule schedule = getScheduleOwnedByCurrentUser(
                scheduleId,
                currentUser.getId()
        );

        schedule.setStatus(ScheduleStatus.CANCELLED);

        paymentScheduleRepository.save(schedule);
    }

    private PaymentSchedule getScheduleOwnedByCurrentUser(Long scheduleId, Long userId) {
        return paymentScheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Payment schedule not found"));
    }

    private void validateWorkerForSchedule(Worker worker) {
        if (worker.getStatus() != WorkerStatus.ACTIVE) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot create schedule for inactive worker");
        }

        if (!worker.isPaymentDetailsVerified()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Worker payment details must be added before creating a schedule"
            );
        }
    }

    private void validateCreateRequest(CreatePaymentScheduleRequest request, Worker worker) {
        validateAmount(request.amount());
        validateReminderDays(request.reminderDaysBefore() == null ? 1 : request.reminderDaysBefore());
        validateNextDueDate(request.firstDueDate(), request.startDate());

        if (request.endDate() != null) {
            validateEndDate(request.endDate(), request.firstDueDate());
        }

        if (request.currency() != worker.getCurrency()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Schedule currency must match worker preferred currency"
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Amount must be greater than zero");
        }
    }

    private void validateNextDueDate(LocalDate nextDueDate, LocalDate startDate) {
        if (nextDueDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Next due date cannot be before start date"
            );
        }
    }

    private void validateEndDate(LocalDate endDate, LocalDate nextDueDate) {
        if (endDate.isBefore(nextDueDate)) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "End date cannot be before next due date"
            );
        }
    }

    private void validateReminderDays(Integer reminderDaysBefore) {
        if (reminderDaysBefore == null || reminderDaysBefore < 0) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Reminder days before cannot be negative"
            );
        }
    }

    private PaymentScheduleResponse mapToResponse(PaymentSchedule schedule) {
        return new PaymentScheduleResponse(
                schedule.getId(),
                schedule.getWorker().getId(),
                schedule.getWorker().getFullName(),
                schedule.getScheduleName(),
                schedule.getAmount(),
                schedule.getCurrency(),
                schedule.getFrequency(),
                schedule.getPaymentMode(),
                schedule.getStartDate(),
                schedule.getNextDueDate(),
                schedule.getEndDate(),
                schedule.getReminderDaysBefore(),
                schedule.getStatus(),
                schedule.getCreatedDate(),
                schedule.getLastModifiedDate()
        );
    }
}
