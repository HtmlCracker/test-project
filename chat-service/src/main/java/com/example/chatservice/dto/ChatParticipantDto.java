package com.example.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatParticipantDto {
    private UUID userId;
    private String name;
    private String surname;
    private String email;
    private LocalDateTime joinedAt;
}
