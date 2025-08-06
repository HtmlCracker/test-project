package com.example.chatservice.controller;

import com.example.chatservice.dto.ChatDto;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.state.ChatType;
import com.example.chatservice.util.JwtUtil;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final JwtUtil jwtUtil;

    public ChatController(ChatMessageService chatMessageService, JwtUtil jwtUtil) {
        this.chatMessageService = chatMessageService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDto> getChatInfo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID chatId) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);

        ChatDto chatDto = chatMessageService.getChatInfo(chatId, userId);
        return ResponseEntity.ok(chatDto);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ChatDto>> getMyChats(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);

        List<ChatDto> chats = chatMessageService.getUserChats(userId);
        return ResponseEntity.ok(chats);
    }

    @PostMapping
    public ResponseEntity<UUID> createChat(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateChatRequest request) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);

        UUID chatId = chatMessageService.createChat(
                request.getParticipantIds(),
                request.getName(),
                request.getType(),
                userId);
        return ResponseEntity.ok(chatId);
    }

    @PostMapping("/{chatId}/participants")
    public ResponseEntity<Void> addParticipant(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID chatId,
            @RequestBody UUID newParticipantId) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);

        chatMessageService.addParticipant(chatId, userId, newParticipantId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{chatId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID chatId,
            @PathVariable UUID participantId) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);

        chatMessageService.removeParticipant(chatId, userId, participantId);
        return ResponseEntity.ok().build();
    }

    @Data
    private static class CreateChatRequest {
        private List<UUID> participantIds;
        private String name;
        private ChatType type;
    }
}
