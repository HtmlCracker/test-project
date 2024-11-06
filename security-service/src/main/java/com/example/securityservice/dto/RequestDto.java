package com.example.securityservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class RequestDto {
    @Email(message = "Email should be valid")
    private String email;
    @NotNull(message = "Password shouldn't be empty")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
