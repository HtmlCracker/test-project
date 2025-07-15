package com.example.chatservice.websocket;

import com.example.chatservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.Map;
import java.util.UUID;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeHandler {

    @Autowired
    private final JwtUtil jwtUtil;

    public WebSocketHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean doHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Map<String, Object> attributes) throws HandshakeFailureException {
        String token = extractTokenFromQuery(request.getURI().getQuery());

        if (token == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            UUID userId = jwtUtil.extractUserId(token);
            if (userId == null || jwtUtil.isTokenExpired(token)) {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return false;
            }

            attributes.put("userId", userId);

            // Передаем управление стандартному handshake-обработчику
            return true;
        } catch (Exception e) {
            // Невалидный UUID
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
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