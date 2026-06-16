package com.introtech.introtechservice.dto;


import com.introtech.introtechservice.common.enums.Currency;
import com.introtech.introtechservice.common.enums.WorkerStatus;

import java.time.Instant;

public record WorkerResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        String jobTitle,
        String category,
        String address,
        String notes,
        Currency preferredCurrency,
        String paymentRegion,
        boolean paymentDetailsVerified,
        WorkerStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
