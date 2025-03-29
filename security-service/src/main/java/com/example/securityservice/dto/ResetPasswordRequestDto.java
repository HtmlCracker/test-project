package com.example.securityservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequestDto {
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password is not valid")
    @NotBlank(message = "Password shouldn't be empty")
    String firstPassword;
    String secondPassword;
    @NotBlank(message = "Token shouldn't be empty")
    String token;
}
