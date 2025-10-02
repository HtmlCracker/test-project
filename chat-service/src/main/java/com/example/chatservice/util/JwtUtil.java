package com.example.chatservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final ObjectMapper objectMapper;

    public UUID extractUserId(String token) {
        try {
            String payload = extractPayload(token);
            Map<String, Object> claims = parseClaims(payload);
            String userIdStr = (String) claims.get("userId");
            return UUID.fromString(userIdStr);
        } catch (Exception e) {}
        throw new JwtException("Invalid JWT Token");
    }

    private String extractPayload(String token) throws ParseException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new ParseException("Invalid JWT token format", 0);
        }
        return new String(Base64.getDecoder().decode(parts[1]));
    }

    private Map<String, Object> parseClaims(String payload) throws JsonProcessingException {
        return objectMapper.readValue(payload, Map.class);
    }
}