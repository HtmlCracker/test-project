package com.example.chatservice.controller;

import com.example.chatservice.dto.Message;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.util.JwtUtil;
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
    public ResponseEntity<List<Message>> history(@PathVariable UUID recipientId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID senderId = jwtUtil.extractUserId(token);
        if (recipientId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Message> history = chatMessageService.getChatHistory(senderId, recipientId);
        return ResponseEntity.ok(history);
    }


}
