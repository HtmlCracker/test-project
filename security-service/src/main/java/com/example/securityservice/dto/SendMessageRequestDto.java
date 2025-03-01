package com.example.securityservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendMessageRequestDto {
    @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", message = "Email should be valid")
    @NotNull
    String toMail;
    @NotBlank
    String subject;
    // "mailConfirm" for send mail verify message
    @NotBlank
    String htmlTemplateName;
    // "token": "some_token" for mailConfirm template
    HashMap<String, Object> variables;
}
