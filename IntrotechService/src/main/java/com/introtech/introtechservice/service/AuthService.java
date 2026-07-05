package com.introtech.introtechservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.introtech.authenticationresource.config.SecurityProperties;
import com.introtech.introtechservice.common.AppResponse;
import com.introtech.introtechservice.common.enums.UserRole;
import com.introtech.introtechservice.dto.LoginVO;
import com.introtech.introtechservice.dto.RegisterVO;
import com.introtech.introtechservice.entity.User;
import com.introtech.introtechservice.exceptions.IntrotechException;
import com.introtech.introtechservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SecurityProperties securityProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AuthService(UserRepository userRepository, @Qualifier("customClient") RestClient.Builder builder, SecurityProperties securityProperties) {
        this.userRepository = userRepository;
        this.restClient = builder.baseUrl("http://authentication-service").build();
        this.securityProperties = securityProperties;
        this.objectMapper = new ObjectMapper();
    }


    @Transactional
    public AppResponse<User> register(RegisterVO req) {

        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
            return new AppResponse<>("Email already exists");
        }

        User user = User.builder()
                .email(req.getEmail().toLowerCase())
                .firstName(req.getFirstName().trim().toLowerCase())
                .lastName(req.getLastName().trim().toLowerCase())
                .phoneNumber(req.getPhoneNumber())
                .role(UserRole.USER)
                .build();
        var savedUser = userRepository.save(user);

        // Create request payload for authentication service
        Map<String, Object> registrationPayload = new HashMap<>();
        registrationPayload.put("email", req.getEmail().toLowerCase());
        registrationPayload.put("firstName", req.getFirstName());
        registrationPayload.put("lastName", req.getLastName());
        registrationPayload.put("phoneNumber", req.getPhoneNumber());
        registrationPayload.put("password", req.getPassword());
        registrationPayload.put("roles", List.of(UserRole.USER.name()));
        registrationPayload.put("clientName", securityProperties.clientName());

        // Make POST request to authentication service
        ResponseEntity<AppResponse> authResponse = restClient.post()
                    .uri("/register")
                    .header("Content-Type", "application/json")
                    .body(registrationPayload)
                    .retrieve()
                    .toEntity(AppResponse.class);

        return new AppResponse<>(true, "User registered successfully", savedUser);

    }

    public ResponseEntity<Object> login(LoginVO req) throws IntrotechException, JsonProcessingException {

//        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
//            throw new IntrotechException("User does not exist");
//        }

        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", req.getEmail());
        loginPayload.put("password", req.getPassword());

        ResponseEntity<Object> authResponse = restClient.post().uri("/login").body(loginPayload)
                .retrieve()
                .toEntity(Object.class);

        return authResponse;
    }
}
