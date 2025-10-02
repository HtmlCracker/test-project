package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final RouteValidator routeValidator;
    private final RestTemplate restTemplate;
    Logger log = LoggerFactory.getLogger(AuthFilter.class);

    @Value("${validateUrl}")
    private String validateUrl;

    public AuthFilter(RouteValidator routeValidator, RestTemplate restTemplate) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.restTemplate = restTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (HttpMethod.OPTIONS.equals(request.getMethod())) {
                return chain.filter(exchange);
            }

            if (routeValidator.isSecured.test(request)) {
                String token = extractToken(request);

                if (token == null) {
                    throw new RuntimeException("Missing authorization token!");
                }

                RequestEntity<Void> requestEntity = new RequestEntity<>(
                        HttpMethod.GET,
                        URI.create(validateUrl + token)
                );

                ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
                if ("Invalid token".equals(response.getBody())) {
                    throw new RuntimeException("Invalid token");
                }

                // Добавляем токен в параметры запроса для chat-service
                if (request.getURI().getPath().startsWith("/ws")) {
                    URI newUri = UriComponentsBuilder.fromUri(request.getURI())
                            .queryParam("token", token)
                            .build()
                            .toUri();
                    ServerHttpRequest newRequest = exchange.getRequest().mutate().uri(newUri).build();
                    exchange = exchange.mutate().request(newRequest).build();
                    log.info("New URI: {}", newUri);
                }
            }
            log.info("[GATEWAY] Обрабатываем защищенный запрос: {} {}", request.getMethod(), request.getURI());
            return chain.filter(exchange);
        };
    }

    private String extractToken(ServerHttpRequest request) {
        // Проверяем query параметры для WebSocket
        if (isWebSocketRequest(request)) {
            String token = request.getQueryParams().getFirst("token");
            if (token != null && !token.isEmpty()) {
                return token;
            }
        }

        // Для остального
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isWebSocketRequest(ServerHttpRequest request) {
        return request.getURI().getPath().startsWith("/ws");
    }

    public static class Config{

    }
}
