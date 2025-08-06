package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMemberRepository extends JpaRepository<ChatMember, UUID> {
    List<ChatMember> findByChatId(UUID chatId);
    boolean existsByChatIdAndUserId(UUID chatId, UUID userId);
    List<UUID> findParticipantIdsByChatId(UUID chatId);
    void deleteByChatIdAndUserId(UUID chatId, UUID participantId);
    List<UUID> findChatIdsByUserId(UUID userId);
}