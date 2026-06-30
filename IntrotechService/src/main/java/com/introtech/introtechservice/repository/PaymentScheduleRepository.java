package com.introtech.introtechservice.repository;

import com.introtech.introtechservice.common.BaseRepository;
import com.introtech.introtechservice.common.enums.ScheduleStatus;
import com.introtech.introtechservice.entity.PaymentSchedule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentScheduleRepository extends BaseRepository<PaymentSchedule, Long> {

    List<PaymentSchedule> findByUserIdAndStatusNotOrderByCreatedDateDesc(
            Long userId,
            ScheduleStatus status
    );

    Optional<PaymentSchedule> findByIdAndUserId(Long id, Long userId);

    List<PaymentSchedule> findByWorkerIdAndUserIdOrderByCreatedDateDesc(
            Long workerId,
            Long userId
    );

    @Query("""
            SELECT s FROM PaymentSchedule s
            WHERE s.status = :status
            AND s.nextDueDate <= :today
            AND (s.endDate IS NULL OR s.nextDueDate <= s.endDate)
            """)
    List<PaymentSchedule> findDueSchedules(
            @Param("status") ScheduleStatus status,
            @Param("today") LocalDate today
    );
}