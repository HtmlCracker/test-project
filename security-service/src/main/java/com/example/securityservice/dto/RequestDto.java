package com.example.securityservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RequestDto {
    @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", message = "Email should be valid")
    private String email;
    @NotBlank(message = "Password shouldn't be empty")
    private String password;

    public RequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public RequestDto() {
    }

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
