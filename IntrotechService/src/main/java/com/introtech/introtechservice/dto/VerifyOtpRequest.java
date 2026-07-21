package com.introtech.introtechservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
        @Email(message = "Enter a valid email")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Otp is required")
        String otp
) {
}
