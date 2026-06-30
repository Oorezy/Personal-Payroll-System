package com.introtech.introtechservice.repository;

import com.introtech.introtechservice.common.BaseRepository;
import com.introtech.introtechservice.common.enums.PaymentStatus;
import com.introtech.introtechservice.entity.PaymentRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRecordRepository extends BaseRepository<PaymentRecord, Long> {
    boolean existsByScheduleIdAndDueDate(Long scheduleId, LocalDate dueDate);

    Optional<PaymentRecord> findByIdAndUserId(Long id, Long userId);

    List<PaymentRecord> findByUserIdOrderByDueDateDesc(Long userId);

    List<PaymentRecord> findByUserIdAndStatusOrderByDueDateDesc(
            Long userId,
            PaymentStatus status
    );

    List<PaymentRecord> findByWorkerIdAndUserIdOrderByDueDateDesc(
            Long workerId,
            Long userId
    );
}