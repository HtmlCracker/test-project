package com.example.chatservice.websocket;

import com.example.chatservice.dto.Message;
import com.example.chatservice.dto.ReadReceipt;
import com.example.chatservice.kafka.KafkaProducer;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.state.MessageState;
import com.example.chatservice.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<UUID, ReadReceipt> readReceiptKafkaTemplate;

    // Хранение активных сессий по uuid пользователя
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(KafkaProducer kafkaProducer, ObjectMapper objectMapper, KafkaTemplate<UUID, ReadReceipt> readReceiptKafkaTemplate) {
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
        this.readReceiptKafkaTemplate = readReceiptKafkaTemplate;
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
        String payload = message.getPayload();
        UUID senderId = (UUID) session.getAttributes().get("userId");

        if (payload.equals("read_receipt:")) {
            // Парсинг JSON в ReadReceipt
            ReadReceipt readReceipt = parseReadReceipt(payload);
            // В данном случае клиент "читает сообщение", для которого он получатель
            readReceiptKafkaTemplate.send("read_receipts", senderId, readReceipt);
        } else {
            // Парсинг JSON в Message
            Message msg = parseMessage(message.getPayload(), senderId);
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
    public void sendMessageToUser(UUID recipientId, Message message) {
        WebSocketSession session = sessions.get(recipientId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(message)));
            } catch (IOException e) {
                message.setStatus(MessageState.FAILED);
            }
        }
    }

    public void sendReadReceipt(UUID recipientId, UUID messageId) {
        ReadReceipt receipt = new ReadReceipt();
        receipt.setMessageId(messageId);
        receipt.setRecipientId(recipientId);
        receipt.setTimestamp(LocalDateTime.now());

        readReceiptKafkaTemplate.send("read_receipts", recipientId, receipt);
    }

    private Message parseMessage(String json, UUID senderId) throws IOException {
        Message message = objectMapper.readValue(json, Message.class);
        message.setSenderId(senderId);
        message.setTimestamp(java.time.LocalDateTime.now());
        return message;
    }

    private ReadReceipt parseReadReceipt(String json) throws IOException {
        ReadReceipt readReceipt = objectMapper.readValue(json, ReadReceipt.class);
        return readReceipt;
    }
}