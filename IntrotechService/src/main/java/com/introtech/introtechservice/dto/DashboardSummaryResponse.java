package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        long totalWorkers,
        long activeWorkers,
        long totalSchedules,
        long activeSchedules,
        long awaitingApprovalPayments,
        long processingPayments,
        long failedPayments,
        Map<Currency, BigDecimal> paidThisMonth,
        List<PaymentRecordResponse> upcomingPayments,
        List<PaymentRecordResponse> recentPayments
) {
}
