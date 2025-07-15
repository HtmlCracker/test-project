package com.example.chatservice.websocket;

import com.example.chatservice.dto.Message;
import com.example.chatservice.kafka.KafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    // Хранение активных сессий по email пользователя
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(KafkaProducer kafkaProducer, ObjectMapper objectMapper) {
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = (UUID) session.getAttributes().get("userId");
        if (userId == null) {
            session.close();
            return;
        }
        // Сохраняем сессию пользователя
        sessions.put(userId, session);
        session.sendMessage(new TextMessage("Connected as " + userId));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UUID senderId = (UUID) session.getAttributes().get("userId");

        // Парсинг JSON в Message
        Message msg = parseMessage(message.getPayload(), senderId);

        // Отправка сообщения через Kafka
        kafkaProducer.sendMessage(msg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        UUID userId = (UUID) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    private Message parseMessage(String json, UUID senderId) throws IOException {
        Message message = objectMapper.readValue(json, Message.class);
        message.setSenderId(senderId);
        message.setTimestamp(java.time.LocalDateTime.now());
        return message;
    }

    // Метод для отправки сообщения пользователю напрямую из KafkaConsumer
    public void sendMessageToUser(UUID recipientId, Message message) {
        WebSocketSession session = sessions.get(recipientId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(message)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}