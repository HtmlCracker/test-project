package com.example.gateway.filter;

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

            if (routeValidator.isSecured.test(request)) {
                String token = extractTokenFromHeaders(request);

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
                    exchange.getRequest().mutate().uri(newUri);
                }
            }

            return chain.filter(exchange);
        };
    }

    private String extractTokenFromHeaders(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    public static class Config{

    }
}
