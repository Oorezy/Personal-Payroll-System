package com.introtech.introtechservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Size(max = 100, message = "First name must be 100 characters or fewer")
        String firstName,

        @Size(max = 100, message = "Last name must be 100 characters or fewer")
        String lastName,

        @Pattern(regexp = "^$|^[0-9+()\\-\\s]{7,30}$", message = "Phone number format is invalid")
        String phoneNumber
) {
}
