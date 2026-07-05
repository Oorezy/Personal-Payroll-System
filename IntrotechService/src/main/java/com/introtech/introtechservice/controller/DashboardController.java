package com.introtech.introtechservice.controller;

import com.introtech.introtechservice.dto.DashboardSummaryResponse;
import com.introtech.introtechservice.dto.MonthlyPayrollReportResponse;
import com.introtech.introtechservice.service.DashboardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public DashboardSummaryResponse getDashboard() {
        return dashboardService.getSummary();
    }

    @GetMapping("/reports/monthly")
    public MonthlyPayrollReportResponse getMonthlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month
    ) {
        YearMonth currentMonth = YearMonth.now();
        int reportYear = year == null ? currentMonth.getYear() : year;
        int reportMonth = month == null ? currentMonth.getMonthValue() : month;
        return dashboardService.getMonthlyReport(reportYear, reportMonth);
    }
}
