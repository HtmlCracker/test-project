package com.example.chatservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Message {
    private UUID messageId;
    private UUID senderId;
    private UUID recipientId;
    private String text;
    private LocalDateTime timestamp;
}