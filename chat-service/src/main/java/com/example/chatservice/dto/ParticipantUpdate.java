package com.example.chatservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ParticipantUpdate {
    private UUID chatId;
    private UUID participantId;

    public enum ParticipantAction {
        JOINED, LEFT, REMOVED
    }

    private ParticipantAction action;
}
