package com.introtech.introtechservice.controller;

import com.introtech.introtechservice.dto.UpdateUserProfileRequest;
import com.introtech.introtechservice.dto.UserProfileResponse;
import com.introtech.introtechservice.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public UserProfileResponse getProfile() {
        return userProfileService.getProfile();
    }

    @PutMapping
    public UserProfileResponse updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userProfileService.updateProfile(request);
    }
}
