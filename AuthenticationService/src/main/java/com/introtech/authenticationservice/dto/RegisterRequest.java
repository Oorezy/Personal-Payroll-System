package com.introtech.authenticationservice.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Data
public class RegisterRequest {

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @Digits(integer = 15, fraction = 0, message = "Phone number must be numeric and maximum of 15 digits")
    private String phoneNumber;

    @Length(min = 8, message = "Password must be at least 8 characters long")
    @NotNull(message = "Password cannot be null")
    private String password;

    private Set<String> roles;

    @NotBlank(message = "Client name cannot be blank")
    private String clientName;
}
