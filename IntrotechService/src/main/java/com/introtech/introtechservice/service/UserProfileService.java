package com.introtech.introtechservice.service;

import com.introtech.introtechservice.dto.UpdateUserProfileRequest;
import com.introtech.introtechservice.dto.UserProfileResponse;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserContextService userContextService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile() {
        return mapToResponse(userContextService.getCurrentUser());
    }

    @Transactional
    public UserProfileResponse updateProfile(UpdateUserProfileRequest request) {
        User user = userContextService.getCurrentUser();

        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName().trim());
        }

        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName().trim());
        }

        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber().trim());
        }

        return mapToResponse(userRepository.save(user));
    }

    private UserProfileResponse mapToResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedDate(),
                user.getLastModifiedDate()
        );
    }
}
