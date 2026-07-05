package com.introtech.introtechservice.dto;

import com.introtech.introtechservice.common.enums.UserRole;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        UserRole role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
