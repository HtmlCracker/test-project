package com.example.chatservice.repository;

import com.example.chatservice.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.userId = :userId")
    List<Chat> findByParticipantId(@Param("userId") UUID userId);

    @Query("SELECT c FROM Chat c " +
            "JOIN ChatMember cm1 ON c.id = cm1.chat.id " +
            "JOIN ChatMember cm2 ON c.id = cm2.chat.id " +
            "WHERE cm1.userId = :userId1 AND cm2.userId = :userId2 AND c.type = 'DIRECT'")
    Optional<Chat> findDirectChatBetweenUsers(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);
}