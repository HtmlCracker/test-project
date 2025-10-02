package com.example.chatservice.websocket;

import com.example.chatservice.dto.EditNotification;
import com.example.chatservice.dto.Message;
import com.example.chatservice.dto.ReadReceipt;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.kafka.KafkaProducer;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.state.MessageState;
import com.example.chatservice.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        UUID userId = (UUID) session.getAttributes().get("userId");

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);

            if (!jsonNode.has("type")) {
                throw new IllegalArgumentException("Missing 'type' field in message");
            }

            String messageType = jsonNode.get("type").asText();

            ((ObjectNode) jsonNode).remove("type");
            switch (messageType) {
                case "message" -> handleMessage(jsonNode, userId);
                case "read_receipt" -> handleReadReceipt(jsonNode, userId);
                default -> {
                    log.warn("Unknown message type received: {}", messageType);
                    throw new IllegalArgumentException("Unknown message type: " + messageType);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON format: {}", payload, e);
            throw new IllegalArgumentException("Invalid message format", e);
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
                log.error("ПРОЕБАЛИ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
    }

    public boolean isUserOnline(UUID userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    public void sendReadReceipt(UUID userId, ReadReceipt receipt) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(receipt)));
            } catch (IOException e) {
                log.error("ПРОЕБАЛИ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
    }

    //на случай различной логики в дальнейшем
    public void sendEditNotification(UUID recipientId, Message notification) {
        sendMessageToUser(recipientId, notification);
    }

    public void sendParticipantNotification(UUID recipientId, Message notification) {
        sendMessageToUser(recipientId, notification);
    }

    private void handleMessage(JsonNode json, UUID senderId) {
        try {
            Message message = objectMapper.treeToValue(json, Message.class);
            message.setSenderId(senderId);
            message.setTimestamp(LocalDateTime.now());
            kafkaProducer.sendMessage(message);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid message format", e);
        }
    }

    private void handleReadReceipt(JsonNode jsonNode, UUID userId) {
        try {
            ReadReceipt receipt = objectMapper.treeToValue(jsonNode, ReadReceipt.class);
            receipt.setRecipientId(userId);
            receipt.setTimestamp(LocalDateTime.now());
            kafkaProducer.sendReadReceipt(receipt);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid read receipt", e);
        }
    }


}