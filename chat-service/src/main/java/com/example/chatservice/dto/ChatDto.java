package com.example.chatservice.dto;

import com.example.chatservice.state.ChatType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ChatDto {
    private UUID id;
    private ChatType type;
    private String name;
    private LocalDateTime createdAt;
    private List<ChatParticipantDto> participants;
    private Message lastMessage;
}
