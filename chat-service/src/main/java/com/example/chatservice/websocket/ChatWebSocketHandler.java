package com.example.chatservice.websocket;

import com.example.chatservice.dto.Message;
import com.example.chatservice.dto.ReadReceipt;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.kafka.KafkaProducer;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.state.MessageState;
import com.example.chatservice.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final KafkaProducer kafkaProducer;
    private final ChatMessageService chatMessageService;

    // Хранение активных сессий по uuid пользователя
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = (UUID) session.getAttributes().get("userId");
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing userId in session attributes"));
            return;
        }
        // Сохраняем сессию пользователя
        sessions.put(userId, session);
        session.sendMessage(new TextMessage("Connected as " + userId));

        // Загрузка всех непрочитанных сообщений
        List<Message> unreadMessages = chatMessageService.getUnreadMessages(userId);
        for (Message message : unreadMessages) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(message)));
            chatMessageService.markAsRead(message.getMessageId(), userId);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        UUID userId = (UUID) session.getAttributes().get("userId");

        if (payload.equals("read_receipt:")) {
            // Парсинг JSON в ReadReceipt
            ReadReceipt readReceipt = parseReadReceipt(payload, userId);
            // В данном случае клиент "читает сообщение", для которого он получатель
            kafkaProducer.sendReadReceipt(readReceipt);
        } else {
            // Парсинг JSON в Message
            Message msg = parseMessage(message.getPayload(), userId);
            // Отправка сообщения через Kafka
            kafkaProducer.sendMessage(msg);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UUID userId = (UUID) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    // Метод для отправки сообщения пользователю напрямую из KafkaConsumer
    public void sendMessageToUser(UUID userId, Message message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(message)));
                chatMessageService.markAsDelivered(message.getMessageId());
            } catch (IOException e) {
                chatMessageService.markAsFailed(message.getMessageId());
            }
        }
    }

    public boolean isUserOnline(UUID userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    //на случай различной логики в дальнейшем
    public void sendEditNotification(UUID recipientId, Message notification) {
        sendMessageToUser(recipientId, notification);
    }

    public void sendParticipantNotification(UUID recipientId, Message notification) {
        sendMessageToUser(recipientId, notification);
    }

    private Message parseMessage(String json, UUID senderId) {
        try {
            Message message = objectMapper.readValue(json, Message.class);
            message.setSenderId(senderId);
            message.setTimestamp(java.time.LocalDateTime.now());
            return message;
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid message format", e);
        }
    }

    private ReadReceipt parseReadReceipt(String payload, UUID userId) {
        try {
            String json = payload.replaceFirst("read_receipt:", "").trim();
            ReadReceipt readReceipt = objectMapper.readValue(json, ReadReceipt.class);
            readReceipt.setRecipientId(userId);
            return readReceipt;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid read receipt", e);
        }
    }
}