package com.introtech.introtechutil.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequest(
        @Email(message = "Enter a valid email")
        @NotBlank(message = "Email is required")
        String email
) {
}