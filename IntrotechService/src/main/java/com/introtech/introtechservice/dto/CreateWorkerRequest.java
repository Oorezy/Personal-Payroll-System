package com.introtech.introtechservice.dto;


import com.introtech.introtechservice.common.enums.Currency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWorkerRequest(

        @NotBlank(message = "Worker full name is required")
        String fullName,

        @Email(message = "Invalid email format")
        String email,

        String phoneNumber,

        String jobTitle,

        String category,

        String address,

        String notes,

        @NotNull(message = "Preferred currency is required")
        Currency preferredCurrency,

        String paymentRegion
) {
}
