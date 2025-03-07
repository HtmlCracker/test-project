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
    String toMail;
    String subject;
    // "mailConfirm" for send mail verify message
    String htmlTemplateName;
    int priority;
    // "token": "some_token" for mailConfirm template
    HashMap<String, Object> variables;
}
