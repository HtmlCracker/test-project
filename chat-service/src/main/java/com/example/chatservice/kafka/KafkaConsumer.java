package com.example.chatservice.kafka;

import com.example.chatservice.dto.Message;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final ChatWebSocketHandler webSocketHandler;
    private final ChatMessageService chatMessageService;


    @KafkaListener(topics = "direct_messages", groupId = "chat-group", containerFactory = "messageKafkaListenerFactory")
    public void consumeMessage(ConsumerRecord<UUID, Message> record, Acknowledgment acknowledgment) {

        Message message = record.value();
        try {
            // Получаем recipientId из метаданных корреляции
            byte[] correlationBytes = record.headers().lastHeader("correlationId").value();
            UUID recipientId = UUID.fromString(new String(correlationBytes, StandardCharsets.UTF_8));

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