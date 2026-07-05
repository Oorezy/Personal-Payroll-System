package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record MonthlyPayrollReportResponse(
        int year,
        int month,
        long paymentCount,
        long paidCount,
        long failedCount,
        long pendingCount,
        Map<Currency, BigDecimal> scheduledTotals,
        Map<Currency, BigDecimal> paidTotals,
        List<PaymentRecordResponse> payments
) {
}
