package com.example.chatservice.kafka;

import com.example.chatservice.dto.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<UUID, Message> kafkaTemplate;

    public void sendMessage(Message message) {
        kafkaTemplate.send("direct_messages", message.getRecipientId(), message);
    }
}