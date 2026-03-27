package com.ykw.api.gateway.filter;

import com.ykw.api.gateway.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TokenBlacklistFilter implements GlobalFilter, Ordered {

    private final CacheService tokenBlacklistService;

    /**
     * 1. Get authenticated user (async)
     * 2. If present:
     *     - Extract JWT
     *     - Get jti
     *     - Check Redis (async)
     *         - If blacklisted ->reject
     *         - Else -> forward request
     * 3. If no user:
     *     - just continue (public route)
     * @param exchange the current server exchange
     * @param chain provides a way to delegate to the next filter
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {

                    var jwt = auth.getToken();
                    String jti = jwt.getId();
                    Long userId = jwt.getClaim("userId");

                    if (jti == null) {
                        return unauthorized(exchange);
                    }

                    return tokenBlacklistService.isTokenRevoked(jti)
                            .onErrorResume(e -> Mono.just(false)) // fail-open
                            .flatMap(isBlacklisted -> {

                                if (Boolean.TRUE.equals(isBlacklisted)) {
                                    return unauthorized(exchange);
                                }

                                ServerHttpRequest mutated = exchange.getRequest()
                                        .mutate()
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-JTI", jti)
                                        .build();

                                return chain.filter(exchange.mutate().request(mutated).build());
                            });
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete().then(Mono.empty());
    }

    @Override
    public int getOrder() {
        return 1;
    }
}