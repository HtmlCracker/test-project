package com.example.chatservice.controller;

import com.example.chatservice.dto.Message;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.util.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final JwtUtil jwtUtil;

    public ChatMessageController(ChatMessageService chatMessageService, JwtUtil jwtUtil) {
        this.chatMessageService = chatMessageService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/history/{recipientId}")
    public ResponseEntity<Page<Message>> history(@PathVariable UUID recipientId, @RequestHeader("Authorization") String authHeader,
                                                 @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        String token = authHeader.substring(7);
        UUID senderId = jwtUtil.extractUserId(token);
        if (recipientId == null) {
            return ResponseEntity.badRequest().build();
        }
        Page<Message> history = chatMessageService.getChatHistory(senderId, recipientId, page, size);
        return ResponseEntity.ok(history);
    }

    @PatchMapping("/edit/{messageId}")
    public ResponseEntity<Message> editMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID messageId,
            @RequestBody String newText) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);

        Message message = chatMessageService.editMessage(messageId, userId, newText);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID messageId) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);

        chatMessageService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}
