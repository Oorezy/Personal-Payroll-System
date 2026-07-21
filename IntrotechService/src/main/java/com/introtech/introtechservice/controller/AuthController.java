package com.introtech.introtechservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.introtech.introtechservice.common.AppResponse;
import com.introtech.introtechservice.dto.LoginVO;
import com.introtech.introtechservice.dto.RegisterVO;
import com.introtech.introtechservice.dto.VerifyOtpRequest;
import com.introtech.introtechservice.exceptions.IntrotechException;
import com.introtech.introtechservice.service.AuthService;
import com.introtech.introtechutil.dto.ResendOtpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@RequiredArgsConstructor
@RequestMapping()
@RestController
public class AuthController {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterVO req) throws IntrotechException, JsonProcessingException {

        try {
            return ResponseEntity.ok(authService.register(req));

        }  catch (HttpClientErrorException.Unauthorized e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(objectMapper.readValue(e.getResponseBodyAsString(), AppResponse.class));
        } catch (HttpClientErrorException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(objectMapper.readValue(e.getResponseBodyAsString(), AppResponse.class));
        }
            catch (Exception e) {
            log.error("login failed", e);
            throw new IntrotechException("Failed to register user");
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginVO req) throws IntrotechException, JsonProcessingException {

        try {
            return authService.login(req);
        } catch (HttpClientErrorException.Unauthorized e) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(objectMapper.readValue(e.getResponseBodyAsString(), AppResponse.class));
        } catch (Exception e) {
            log.error("login failed", e);
            throw new IntrotechException("Failed to login user with authentication service");
        }

    }


    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody @Valid VerifyOtpRequest req) throws IntrotechException, JsonProcessingException {

        try {
            return authService.verifyOtp(req);
        } catch (HttpClientErrorException.Unauthorized e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(objectMapper.readValue(e.getResponseBodyAsString(), AppResponse.class));
        } catch (HttpClientErrorException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(objectMapper.readValue(e.getResponseBodyAsString(), AppResponse.class));
        } catch (Exception e) {
            log.error("otp verification failed", e);
            throw new IntrotechException("Failed to verify email with authentication service");
        }

    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody @Valid ResendOtpRequest req) throws IntrotechException, JsonProcessingException {

        try {
            return authService.resendOtp(req);
        } catch (HttpClientErrorException.Unauthorized e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(objectMapper.readValue(e.getResponseBodyAsString(), AppResponse.class));
        } catch (HttpClientErrorException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(objectMapper.readValue(e.getResponseBodyAsString(), AppResponse.class));
        } catch (Exception e) {
            log.error("otp resend failed", e);
            throw new IntrotechException("Failed to resend verification code");
        }

    }

}
