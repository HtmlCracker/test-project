package com.example.chatservice.kafka;

import com.example.chatservice.dto.Message;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final ChatWebSocketHandler webSocketHandler;
    private final ChatMessageService chatMessageService;

    @KafkaListener(topics = "direct_messages", groupId = "chat-group")
    public void consumeMessage(Message message) {
        // Отправка сообщения через WebSocket получателю
        webSocketHandler.sendMessageToUser(message.getRecipientId(), message);
        chatMessageService.saveMessage(message);
    }
}