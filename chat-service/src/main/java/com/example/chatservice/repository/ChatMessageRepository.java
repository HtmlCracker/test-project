package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findBySenderIdAndRecipientId(UUID senderId, UUID recipientId);
    List<ChatMessage> findBySenderIdOrRecipientId(UUID senderId, UUID recipientId);
}
