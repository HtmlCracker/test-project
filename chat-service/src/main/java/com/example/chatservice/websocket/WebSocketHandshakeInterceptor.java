package com.example.chatservice.websocket;

import com.example.chatservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private final JwtUtil jwtUtil;

    public WebSocketHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Map<String, Object> attributes) throws HandshakeFailureException {
        String token = extractTokenFromQuery(request.getURI().getQuery());

        if (token == null) {
            log.warn("WebSocket handshake rejected: missing or invalid token");
            return false;
        }

        try {
            UUID userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                log.warn("WebSocket handshake rejected: userId not found in token");
                return false;
            }

            attributes.put("userId", userId);
            log.info("WebSocket handshake successful for user: {}", userId);

            // Передаем управление стандартному handshake-обработчику
            return true;
        } catch (Exception e) {
            // Невалидный UUID
            log.error("Error validating token during WebSocket handshake", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake failed: {}", exception.getMessage());
        }
    }

    private String extractTokenFromQuery(String query) {
        if (query == null)
            return null;
        return extractFromQuery(query, "token");
    }

    private String extractFromQuery(String query, String key) {
        for (String param : query.split("&")) {
            if (param.startsWith(key + "=")) {
                return param.split("=")[1];
            }
        }
        return null;
    }
}