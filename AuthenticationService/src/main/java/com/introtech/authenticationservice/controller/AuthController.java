package com.introtech.authenticationservice.controller;

import com.introtech.authenticationservice.UserDetailsImpl;
import com.introtech.authenticationservice.entity.AuthUser;
import com.introtech.authenticationservice.jwt.JwtService;
import com.introtech.authenticationservice.repository.AuthUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
public class AuthController {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @GetMapping("/test")
    public String index() {
        System.out.println("Hello World");
        var principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        System.out.println("principal " + principal);
        return "Hello World";
    }

    @PostMapping("/register")
    public String register(@RequestBody AuthUser user) {

        if (authUserRepository.findByEmailIgnoreCase(user.getEmail()) != null) {
            return "User already exists";
        }
        user.setPassword_hash(passwordEncoder.encode(user.getPassword_hash()));
        authUserRepository.save(user);
        return "User registered successfully";
    }

    @PostMapping("/login") //TODO: Change return object
    public Object verify(@RequestParam("username") String username, @RequestParam("password") String password_hash) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password_hash));

            if (authentication.isAuthenticated()) {
                System.out.println( "User logged in successfully");
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                return jwtService.generateToken(userDetails);
            }else
                return "Invalid email or password";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/refresh-token")
    public Object refreshToken(@RequestParam("token") String token) {
        if (jwtService.validateRefreshToken(token)){
            var userDetails = jwtService.getUserDetailsFromToken(token);
            return jwtService.generateToken(userDetails);
        }
        return "Error refreshing token";
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
