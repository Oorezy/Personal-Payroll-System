package com.introtech.introtechservice.service;

import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.PaymentStatus;
import com.introtech.introtechservice.common.enums.ScheduleStatus;
import com.introtech.introtechservice.common.enums.WorkerStatus;
import com.introtech.introtechservice.dto.DashboardSummaryResponse;
import com.introtech.introtechservice.dto.MonthlyPayrollReportResponse;
import com.introtech.introtechservice.dto.PaymentRecordResponse;
import com.introtech.introtechservice.entity.PaymentRecord;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.mappers.PaymentRecordMapper;
import com.introtech.introtechservice.repository.PaymentRecordRepository;
import com.introtech.introtechservice.repository.PaymentScheduleRepository;
import com.introtech.introtechservice.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final Set<PaymentStatus> UPCOMING_STATUSES = Set.of(
            PaymentStatus.SCHEDULED,
            PaymentStatus.DUE,
            PaymentStatus.AWAITING_APPROVAL,
            PaymentStatus.OVERDUE,
            PaymentStatus.PROCESSING
    );

    private final UserContextService userContextService;
    private final WorkerRepository workerRepository;
    private final PaymentScheduleRepository paymentScheduleRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentRecordMapper paymentRecordMapper;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        User user = userContextService.getCurrentUser();
        Long userId = user.getId();
        YearMonth currentMonth = YearMonth.now();

        List<PaymentRecord> monthlyPayments = getPaymentsForMonth(userId, currentMonth);

        return new DashboardSummaryResponse(
                workerRepository.countByUserIdAndStatusNot(userId, WorkerStatus.ARCHIVED),
                workerRepository.countByUserIdAndStatus(userId, WorkerStatus.ACTIVE),
                paymentScheduleRepository.countByUserIdAndStatusNot(userId, ScheduleStatus.CANCELLED),
                paymentScheduleRepository.countByUserIdAndStatus(userId, ScheduleStatus.ACTIVE),
                paymentRecordRepository.countByUserIdAndStatus(userId, PaymentStatus.AWAITING_APPROVAL),
                paymentRecordRepository.countByUserIdAndStatus(userId, PaymentStatus.PROCESSING),
                paymentRecordRepository.countByUserIdAndStatus(userId, PaymentStatus.FAILED),
                totalsByCurrency(monthlyPayments, true),
                mapPayments(paymentRecordRepository.findTop5ByUserIdAndStatusInOrderByDueDateAsc(
                        userId,
                        UPCOMING_STATUSES
                )),
                mapPayments(paymentRecordRepository.findTop5ByUserIdOrderByCreatedDateDesc(userId))
        );
    }

    @Transactional(readOnly = true)
    public MonthlyPayrollReportResponse getMonthlyReport(int year, int month) {
        YearMonth reportMonth = YearMonth.of(year, month);
        Long userId = userContextService.getCurrentUser().getId();
        List<PaymentRecord> payments = getPaymentsForMonth(userId, reportMonth);

        long paidCount = countStatus(payments, PaymentStatus.PAID);
        long failedCount = countStatus(payments, PaymentStatus.FAILED);
        long pendingCount = payments.stream()
                .filter(payment -> UPCOMING_STATUSES.contains(payment.getStatus()))
                .count();

        return new MonthlyPayrollReportResponse(
                year,
                month,
                payments.size(),
                paidCount,
                failedCount,
                pendingCount,
                totalsByCurrency(payments, false),
                totalsByCurrency(payments, true),
                mapPayments(payments)
        );
    }

    private List<PaymentRecord> getPaymentsForMonth(Long userId, YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        return paymentRecordRepository.findByUserIdAndDueDateBetweenOrderByDueDateDesc(
                userId,
                startDate,
                endDate
        );
    }

    private long countStatus(List<PaymentRecord> payments, PaymentStatus status) {
        return payments.stream().filter(payment -> payment.getStatus() == status).count();
    }

    private Map<Currency, BigDecimal> totalsByCurrency(
            List<PaymentRecord> payments,
            boolean paidOnly
    ) {
        Map<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
        for (Currency currency : Currency.values()) {
            totals.put(currency, BigDecimal.ZERO);
        }

        payments.stream()
                .filter(payment -> !paidOnly || payment.getStatus() == PaymentStatus.PAID)
                .forEach(payment -> totals.merge(
                        payment.getCurrency(),
                        payment.getAmount(),
                        BigDecimal::add
                ));

        return totals;
    }

    private List<PaymentRecordResponse> mapPayments(List<PaymentRecord> payments) {
        return payments.stream().map(paymentRecordMapper::toDto).toList();
    }
}
