package com.example.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
        return (((exchange, chain) -> {
            if (routeValidator.isSecured.test(exchange.getRequest())){

                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing authorization header!");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

                if (authHeader!=null && authHeader.startsWith("Bearer "))
                    authHeader = authHeader.substring(7);

                RequestEntity<Void> requestEntity = new RequestEntity<>(HttpMethod.GET, URI.create(validateUrl + authHeader));
                ResponseEntity<String> response = restTemplate.exchange(requestEntity,String.class);
                if (response.getBody().equals("Invalid token"))
                    throw new RuntimeException("Invalid token");
            }

            return chain.filter(exchange);
        }));
    }

    public static class Config{

    }
}
