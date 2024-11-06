package com.example.securityservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class UserDTO {
    @Email(message = "Email should be valid")
    private String email;
    @NotNull(message = "Password shouldn't be empty")
    private String password;
}
