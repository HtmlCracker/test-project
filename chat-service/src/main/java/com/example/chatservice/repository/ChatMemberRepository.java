package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMemberRepository extends JpaRepository<ChatMember, UUID> {
    List<ChatMember> findByChatId(UUID chatId);
    boolean existsByChatIdAndUserId(UUID chatId, UUID userId);
    @Query("SELECT cm.userId FROM ChatMember cm WHERE cm.chat.id = :chatId")
    List<UUID> findParticipantIdsByChatId(@Param("chatId") UUID chatId);
    void deleteByChatIdAndUserId(UUID chatId, UUID participantId);
    List<UUID> findChatIdsByUserId(UUID userId);
}