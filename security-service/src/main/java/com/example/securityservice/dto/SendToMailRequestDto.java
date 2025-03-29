package com.example.securityservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendToMailRequestDto {
    String toMail;
    HashMap<String, Object> variables;
}
