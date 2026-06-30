package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.Currency;
import jakarta.validation.constraints.Email;

public record UpdateWorkerRequest(

        String fullName,

        @Email(message = "Invalid email format")
        String email,

        String phoneNumber,

        String jobTitle,

        String category,

        String address,

        String notes,

        Currency preferredCurrency,

        String paymentRegion
) {
}
