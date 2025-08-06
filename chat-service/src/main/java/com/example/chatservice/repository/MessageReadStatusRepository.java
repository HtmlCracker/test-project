package com.example.chatservice.repository;

import com.example.chatservice.entity.MessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, UUID> {
    Optional<MessageReadStatus> findByMessageIdAndUserId(UUID messageId, UUID userId);
}