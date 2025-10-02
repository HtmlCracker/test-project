package com.example.chatservice.dto;

import com.example.chatservice.entity.ChatMessage;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Data
public class EditNotification {
    private UUID chatId;
    private UUID messageId;
    private UUID senderId;
    private String newText;
    private LocalDateTime editedAt;

    public Message toMessage() {
        Message message = new Message();
        message.setMessageId(this.messageId);
        message.setText(this.newText);
        message.setEdited(true);
        message.setSenderId(this.senderId);
        message.setChatId(this.chatId);
        message.setEditNotification(true);

        return message;
    }

    public static EditNotification fromMessage(ChatMessage message, String editorName) {
        EditNotification notification = new EditNotification();
        notification.setMessageId(message.getId());
        notification.setNewText(message.getText());
        notification.setSenderId(message.getSenderId());
        notification.setChatId(message.getChatId());
        return notification;
    }
}
