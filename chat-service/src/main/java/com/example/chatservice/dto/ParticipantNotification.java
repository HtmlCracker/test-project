package com.example.chatservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class ParticipantNotification {
    private UUID chatId;
    private UUID participantId;
    private String participantName;
    private LocalDateTime timestamp;

    public enum Action {
        JOINED, LEFT, REMOVED
    }

    private Action action;
    private UUID adminId;


    public Message toMessage() {
        Message message = new Message();
        message.setChatId(this.chatId);
        message.setTimestamp(this.timestamp);
        message.setParticipantNotification(true);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("type", "participant_notification");
        metadata.put("participantId", this.participantId.toString());
        metadata.put("participantName", this.participantName);
        metadata.put("action", this.action.name());

        if (this.adminId != null) {
            metadata.put("adminId", this.adminId.toString());
        }

        message.setMetadata(metadata);
        return message;
    }

    public static ParticipantNotification joined(UUID chatId, UUID participantId,
                                                 String participantName,
                                                 UUID adminId) {
        ParticipantNotification notification = new ParticipantNotification();
        notification.setChatId(chatId);
        notification.setParticipantId(participantId);
        notification.setParticipantName(participantName);
        notification.setTimestamp(LocalDateTime.now());
        notification.setAction(Action.JOINED);
        notification.setAdminId(adminId);
        return notification;
    }


    public static ParticipantNotification removed(UUID chatId, UUID participantId,
                                                  String participantName,
                                                  UUID adminId) {
        ParticipantNotification notification = new ParticipantNotification();
        notification.setChatId(chatId);
        notification.setParticipantId(participantId);
        notification.setParticipantName(participantName);
        notification.setTimestamp(LocalDateTime.now());
        notification.setAction(Action.REMOVED);
        notification.setAdminId(adminId);
        return notification;
    }

    public static ParticipantNotification left(UUID chatId, UUID participantId, String participantName) {
        ParticipantNotification notification = new ParticipantNotification();
        notification.setChatId(chatId);
        notification.setParticipantId(participantId);
        notification.setParticipantName(participantName);
        notification.setTimestamp(LocalDateTime.now());
        notification.setAction(Action.LEFT);
        return notification;
    }
}