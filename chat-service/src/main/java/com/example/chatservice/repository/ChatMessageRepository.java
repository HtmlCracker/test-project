package com.example.chatservice.repository;

import com.example.chatservice.entity.Chat;
import com.example.chatservice.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByChatId(UUID chatId);
    Page<ChatMessage> findByChatId(UUID chatId, Pageable pageable);
    Optional<ChatMessage> findTopByChatIdOrderByTimestampDesc(UUID chatId);
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatId = :chatId " +
            "AND NOT EXISTS (" +
            "    SELECT rs FROM MessageReadStatus rs " +
            "    WHERE rs.messageId = m.id AND rs.userId = :userId" +
            ")")
    List<ChatMessage> findUnreadMessagesForUser(
            @Param("userId") UUID userId,
            @Param("chatId") UUID chatId);

    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatId = :chatId " +
            "AND NOT EXISTS (" +
            "    SELECT rs FROM MessageReadStatus rs " +
            "    WHERE rs.messageId = m.id AND rs.userId = :userId" +
            ")")
    long countUnreadMessagesForUser(
            @Param("userId") UUID userId,
            @Param("chatId") UUID chatId);
}
