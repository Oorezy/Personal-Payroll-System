package com.introtech.authenticationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpRequest(

        @Email(message = "Enter a valid email")
        String email,

        @NotBlank(message = "Otp is required")
        String otp
) {
}
