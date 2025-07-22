package com.example.chatservice.kafka;

import com.example.chatservice.dto.Message;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<UUID, Message> kafkaTemplate;
    private final ChatMessageService chatMessageService;

    public void sendMessage(Message message) {
        ChatMessage chatMessage = chatMessageService.saveMessage(message);
        message.setMessageId(chatMessage.getId());
        kafkaTemplate.send("direct_messages", message.getRecipientId(), message);

    }
}