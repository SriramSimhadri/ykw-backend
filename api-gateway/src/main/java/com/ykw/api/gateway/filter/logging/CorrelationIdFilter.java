package com.ykw.api.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID = "traceId";
    public static final String HEADER = "X-Request-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String traceId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(HEADER)
        ).orElse(UUID.randomUUID().toString());

        // add header to the request
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header(HEADER, traceId)
                .build();

        // store in exchange
        exchange.getAttributes().put(TRACE_ID, traceId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}