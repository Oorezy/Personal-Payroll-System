package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.PaymentFrequency;
import com.introtech.introtechservice.common.enums.PaymentStatus;
import com.introtech.introtechservice.common.enums.ScheduleStatus;
import com.introtech.introtechservice.entity.PaymentRecord;
import com.introtech.introtechservice.entity.PaymentSchedule;
import com.introtech.introtechservice.repository.PaymentRecordRepository;
import com.introtech.introtechservice.repository.PaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentRecordGenerationService {

    private final PaymentScheduleRepository paymentScheduleRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    @Transactional
    public int generateDuePaymentRecords() {
        LocalDate today = LocalDate.now();

        List<PaymentSchedule> dueSchedules = paymentScheduleRepository.findDueSchedules(
                ScheduleStatus.ACTIVE,
                today
        );

        int generatedCount = 0;

        for (PaymentSchedule schedule : dueSchedules) {
            generatedCount += generateRecordsForSchedule(schedule, today);
        }

        return generatedCount;
    }

    private int generateRecordsForSchedule(PaymentSchedule schedule, LocalDate today) {
        int generatedCount = 0;
        int safetyCounter = 0;

        /*
         * This loop handles missed schedules.
         * Example: if the app was offline for 2 months, it can catch up
         * and create the missed due payment records.
         */
        while (
                schedule.getStatus() == ScheduleStatus.ACTIVE
                        && !schedule.getNextDueDate().isAfter(today)
                        && isWithinEndDate(schedule)
        ) {
            safetyCounter++;

            if (safetyCounter > 120) {
                throw new IllegalStateException(
                        "Too many payment records generated for schedule: " + schedule.getId()
                );
            }

            LocalDate dueDate = schedule.getNextDueDate();

            boolean alreadyExists = paymentRecordRepository.existsByScheduleIdAndDueDate(
                    schedule.getId(),
                    dueDate
            );

            if (!alreadyExists) {
                PaymentRecord paymentRecord = PaymentRecord.builder()
                        .user(schedule.getUser())
                        .worker(schedule.getWorker())
                        .schedule(schedule)
                        .amount(schedule.getAmount())
                        .currency(schedule.getCurrency())
                        .dueDate(dueDate)
                        .status(PaymentStatus.AWAITING_APPROVAL)
                        .idempotencyKey(buildIdempotencyKey(schedule, dueDate))
                        .notes("Generated from schedule: " + schedule.getScheduleName())
                        .build();

                paymentRecordRepository.save(paymentRecord);
                generatedCount++;
            }

            advanceSchedule(schedule);
        }

        paymentScheduleRepository.save(schedule);

        return generatedCount;
    }

    private void advanceSchedule(PaymentSchedule schedule) {
        if (schedule.getFrequency() == PaymentFrequency.ONE_TIME) {
            schedule.setStatus(ScheduleStatus.COMPLETED);
            return;
        }

        LocalDate nextDate = calculateNextDueDate(
                schedule.getNextDueDate(),
                schedule.getFrequency()
        );

        if (schedule.getEndDate() != null && nextDate.isAfter(schedule.getEndDate())) {
            schedule.setStatus(ScheduleStatus.COMPLETED);
            return;
        }

        schedule.setNextDueDate(nextDate);
    }

    private LocalDate calculateNextDueDate(
            LocalDate currentDueDate,
            PaymentFrequency frequency
    ) {
        return switch (frequency) {
            case ONE_TIME -> currentDueDate;
            case WEEKLY -> currentDueDate.plusWeeks(1);
            case BI_WEEKLY -> currentDueDate.plusWeeks(2);
            case MONTHLY -> currentDueDate.plusMonths(1);
        };
    }

    private boolean isWithinEndDate(PaymentSchedule schedule) {
        return schedule.getEndDate() == null
                || !schedule.getNextDueDate().isAfter(schedule.getEndDate());
    }

    private String buildIdempotencyKey(PaymentSchedule schedule, LocalDate dueDate) {
        return "payment-" + schedule.getId() + "-" + dueDate;
    }
}
