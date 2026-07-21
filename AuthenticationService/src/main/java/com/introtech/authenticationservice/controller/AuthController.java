package com.introtech.authenticationservice.controller;

import com.introtech.authenticationservice.AppResponse;
import com.introtech.authenticationservice.CustomException;
import com.introtech.authenticationservice.UserDetailsImpl;
import com.introtech.authenticationservice.dto.LoginRequest;
import com.introtech.authenticationservice.dto.OtpRequest;
import com.introtech.authenticationservice.dto.RegisterRequest;
import com.introtech.authenticationservice.entity.AuthUser;
import com.introtech.authenticationservice.entity.Client;
import com.introtech.authenticationservice.entity.UserRoles;
import com.introtech.authenticationservice.jwt.JwtService;
import com.introtech.authenticationservice.jwt.JwtTokenResponse;
import com.introtech.authenticationservice.service.AuthService;
import com.introtech.authenticationservice.service.AuthUserService;
import com.introtech.authenticationservice.service.ClientService;
import com.introtech.authenticationservice.service.UserRoleService;
import com.introtech.introtechutil.dto.ResendOtpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthUserService authUserService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ClientService clientService;
    private final UserRoleService userRoleService;
    private final AuthService authService;

    @PostMapping("/register")
    public AppResponse register(@RequestBody @Valid RegisterRequest request) throws CustomException {

        Client client = clientService.clientExists(request.getClientName());
        if (client != null) {
            if (authUserService.existsByEmailAndClient_ClientName(request.getEmail().toLowerCase(), request.getClientName())) {
                throw new CustomException("Email Already Exists");
            }
            Set<UserRoles> userRoles = userRoleService.getUserRoles(request.getRoles(), client);

            AuthUser user = new AuthUser();
            user.setEmail(request.getEmail().toLowerCase());
            user.setFirstName(request.getFirstName().toLowerCase());
            user.setLastName(request.getLastName().toLowerCase());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setPassword_hash(passwordEncoder.encode(request.getPassword()));
            user.setRoles(userRoles);
            user.setClient(client);

            user = authUserService.save(user);

            authService.sendOtp(user); //Use verification instead
            return new AppResponse(true, "User registered successfully");

        } else
            throw new CustomException("Client does not exist");
    }

    @PostMapping("/login")
    public JwtTokenResponse login(@RequestBody @Valid LoginRequest request) throws CustomException {

        if (!authUserService.isUserVerified(request.getEmail())){
            throw new CustomException("Email not Verified");
        }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            if (!authentication.isAuthenticated()) {
                throw new CustomException("Authentication failed. Please check credentials");
            }
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return jwtService.generateToken(userDetails);
    }

    @PostMapping("/verify")
    public JwtTokenResponse verifyAccount(@RequestBody @Valid OtpRequest request) throws CustomException {

        if (authService.verifyOtp(request.email(), request.otp())) {

            UserDetailsImpl user = authUserService.findUserByEmail(request.email());
            return jwtService.generateToken(user);
        }
        throw new CustomException("Invalid OTP");
    }

    @PostMapping("/resend-otp")
    public AppResponse resendOtp(@RequestBody @Valid ResendOtpRequest request) throws CustomException {
        authService.resendOtp(request.email());
        return new AppResponse(true, "Verification code sent");
    }

    @GetMapping("/test")
    public String index() {
        System.out.println("Hello World");
        var principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        System.out.println("principal " + principal);
        return "Hello World";
    }

    @PostMapping("/refresh-token")
    public Object refreshToken(@RequestParam("token") String token) throws CustomException {
        if (jwtService.validateRefreshToken(token)){
            var userDetails = jwtService.getUserDetailsFromToken(token);
            return jwtService.generateToken(userDetails);
        }
        throw new CustomException("Error refreshing token");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String admin() {
        return "Admin";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public String user() {
        return "User";
    }
}
