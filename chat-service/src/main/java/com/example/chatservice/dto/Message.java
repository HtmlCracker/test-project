package com.example.chatservice.dto;

import com.example.chatservice.state.MessageState;
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
    private MessageState status = MessageState.SENT;
    private LocalDateTime readAt;
    private boolean isEdited = false;
}