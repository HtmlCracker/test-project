package org.example.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.exceptions.JwtException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtUtils {
    ObjectMapper objectMapper;
    public UUID extractUserId(String token) {
        try {
            String payload = extractPayload(token);
            Map<String, Object> claims = parseClaims(payload);
            String userIdStr = (String) claims.get("userId");
            System.out.println(userIdStr);
            return UUID.fromString(userIdStr);
        } catch (Exception e) {}
        throw new JwtException("Invalid JWT Token");
    }

    private String extractPayload(String token) throws ParseException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new ParseException("Invalid JWT token format", 0);
        }
        return new String(Base64.getUrlDecoder().decode(parts[1]));
    }

    private Map parseClaims(String payload) throws JsonProcessingException {
        return objectMapper.readValue(payload, Map.class);
    }
}
