package com.ykw.api.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global gateway filter that logs incoming requests and responses.
 * Adds a traceId to support distributed request tracing.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String TRACE_ID = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        var request = exchange.getRequest();

        // get traceId from header or generate new one
        String traceId = request.getHeaders().getFirst("X-Trace-Id");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        // store traceId in MDC for logging
        MDC.put(TRACE_ID, traceId);

        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String clientIp = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        log.info("Incoming request: method={} path={} clientIp={}", method, path, clientIp);

        // propagate traceId to downstream services
        var mutatedRequest = request.mutate()
                .header("X-Trace-Id", traceId)
                .build();

        var mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        String finalTraceId = traceId;
        return chain.filter(mutatedExchange)
                .doOnSuccess(aVoid -> {
                    MDC.put(TRACE_ID, finalTraceId);   // restore MDC

                    long duration = System.currentTimeMillis() - startTime;

                    var response = exchange.getResponse();

                    log.info("Response: method={} path={} status={} duration={}ms",
                            method,
                            path,
                            response.getStatusCode(),
                            duration);
                })
                .doFinally(signalType -> MDC.remove(TRACE_ID));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}