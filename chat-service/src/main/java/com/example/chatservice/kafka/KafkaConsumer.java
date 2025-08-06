package com.example.chatservice.kafka;

import com.example.chatservice.dto.Message;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final ChatWebSocketHandler webSocketHandler;
    private final ChatMessageService chatMessageService;


    @KafkaListener(topics = "direct_messages", groupId = "chat-group", containerFactory = "messageKafkaListenerFactory")
    public void consumeMessage(Message message, UUID recipientId, Acknowledgment acknowledgment) {
        try {
            // Проверяем, является ли сообщение уведомлением об изменении
            if (message.isEditNotification()) {
                handleEditNotification(message, recipientId, acknowledgment);
                return;
            }
            // Проверяем, является ли сообщение уведомлением об участниках
            if (message.isParticipantNotification()) {
                handleParticipantNotification(message, recipientId, acknowledgment);
                return;
            }
            // Обработка обычного сообщения
            handleRegularMessage(message, recipientId, acknowledgment);
            // Отправка сообщения через WebSocket получателю
            webSocketHandler.sendMessageToUser(recipientId, message);
            chatMessageService.markAsDelivered(message.getMessageId());
        } catch (Exception e) {
            e.printStackTrace();
            chatMessageService.markAsFailed(message.getMessageId());
            acknowledgment.acknowledge();
        }
    }

    private void handleRegularMessage(Message message, UUID recipientId, Acknowledgment acknowledgment) {
        if (webSocketHandler.isUserOnline(recipientId)) {
            webSocketHandler.sendMessageToUser(recipientId, message);
        }
        acknowledgment.acknowledge();
    }

    private void handleEditNotification(Message message, UUID recipientId, Acknowledgment acknowledgment) {
        if (webSocketHandler.isUserOnline(recipientId)) {
            webSocketHandler.sendEditNotification(recipientId, message);
        }
        acknowledgment.acknowledge();
    }

    private void handleParticipantNotification(Message message, UUID recipientId, Acknowledgment acknowledgment) {
        if (webSocketHandler.isUserOnline(recipientId)) {
            webSocketHandler.sendParticipantNotification(recipientId, message);
        }
        acknowledgment.acknowledge();
    }
}