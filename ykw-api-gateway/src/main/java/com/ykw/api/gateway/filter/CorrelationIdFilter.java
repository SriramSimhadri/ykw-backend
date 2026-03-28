package com.ykw.api.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static com.ykw.common.constants.Constants.TRACE_HEADER;
import static com.ykw.common.constants.Constants.TRACE_ID;

/**
 * Filter with the highest priority to capture the traceId/correlationId of a request
 */
@Component
@Order(-2)
public class CorrelationIdFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String traceId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(TRACE_HEADER)
        ).orElse(UUID.randomUUID().toString());

        // add header to the request
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header(TRACE_HEADER, traceId)
                .build();

        // store in exchange
        exchange.getAttributes().put(TRACE_ID, traceId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}