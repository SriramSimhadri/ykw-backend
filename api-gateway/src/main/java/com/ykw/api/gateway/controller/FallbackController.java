package com.ykw.api.gateway.controller;

import com.ykw.api.gateway.dto.FallbackResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
public class FallbackController {

    private Mono<ResponseEntity<FallbackResponse>> buildResponse(
            String serviceName,
            String message,
            ServerWebExchange exchange
    ) {
        FallbackResponse response = FallbackResponse.builder()
                .timestamp(Instant.now())
                .service(serviceName)
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Service Unavailable")
                .message(message)
                .path(exchange.getRequest().getURI().getPath())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @RequestMapping("/fallback/profile")
    public Mono<ResponseEntity<FallbackResponse>> profileFallback(ServerWebExchange exchange) {
        return buildResponse(
                "profile-service",
                "Profile service is temporarily unavailable",
                exchange
        );
    }

    @RequestMapping("/fallback/auth")
    public Mono<ResponseEntity<FallbackResponse>> authFallback(ServerWebExchange exchange) {
        return buildResponse(
                "auth-service",
                "Authentication service is temporarily unavailable",
                exchange
        );
    }

    @RequestMapping("/fallback/articles")
    public Mono<ResponseEntity<FallbackResponse>> articleFallback(ServerWebExchange exchange) {
        return buildResponse(
                "article-service",
                "Article service is temporarily unavailable",
                exchange
        );
    }

    @RequestMapping("/fallback/likes")
    public Mono<ResponseEntity<FallbackResponse>> likesFallback(ServerWebExchange exchange) {
        return buildResponse(
                "likes-service",
                "Likes service is temporarily unavailable",
                exchange
        );
    }

    @RequestMapping("/fallback/comments")
    public Mono<ResponseEntity<FallbackResponse>> commentsFallback(ServerWebExchange exchange) {
        return buildResponse(
                "comments-service",
                "Comments service is temporarily unavailable",
                exchange
        );
    }

    @RequestMapping("/fallback/follows")
    public Mono<ResponseEntity<FallbackResponse>> followsFallback(ServerWebExchange exchange) {
        return buildResponse(
                "follows-service",
                "Follows service is temporarily unavailable",
                exchange
        );
    }
}