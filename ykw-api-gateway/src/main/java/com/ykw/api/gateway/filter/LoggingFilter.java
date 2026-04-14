package com.ykw.api.gateway.filter;

import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.ykw.common.constants.Constants.TRACE_ID;
import static com.ykw.common.constants.Constants.USER_ID;

/**
 * Gateway logging filter to log each request response details
 */
@Component
@Order(-1)
@RequiredArgsConstructor
public class LoggingFilter implements GlobalFilter {

    private final Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        Span span = tracer.spanBuilder("gateway.request").startSpan();

        return chain.filter(exchange)
                .doFirst(() -> {
                    try (Scope scope = span.makeCurrent()) {
                        String traceId = getTraceId();
                        span.setAttribute("http.method", method);
                        span.setAttribute("http.path", path);
                        withMdc(traceId, "anonymous", () ->
                                LogUtil.info(
                                        LogEvent.create("REQUEST_RECEIVED")
                                                .traceId(traceId)
                                                .method(method)
                                                .path(path)
                                )
                        );
                    }
                })
                //on each request success
                .doOnSuccess(aVoid -> {
                    try (Scope scope = span.makeCurrent()) {
                        String traceId = getTraceId();
                        int status = exchange.getResponse().getStatusCode() != null
                                ? exchange.getResponse().getStatusCode().value()
                                : 200;
                        long latency = System.currentTimeMillis() - startTime;
                        span.setAttribute("http.status", status);
                        span.setAttribute("latency.ms", latency);
                        withMdc(traceId, "anonymous", () ->
                                LogUtil.info(
                                        LogEvent.create("REQUEST_COMPLETED")
                                                .traceId(traceId)
                                                .status(status)
                                                .latency(latency)
                                )
                        );
                    }
                })
                //on error
                .doOnError(error -> {
                    try (Scope scope = span.makeCurrent()) {
                        String traceId = getTraceId();
                        span.recordException(error);
                        withMdc(traceId, "anonymous", () ->
                                LogUtil.error(
                                        LogEvent.create("REQUEST_FAILED")
                                                .traceId(traceId)
                                                .error(error.getMessage())
                                )
                        );
                    }
                })
                .doFinally(signal -> span.end());
    }

    private static String getTraceId() {
        return Span.current().getSpanContext().getTraceId();
    }

    /**
     * Wrap traceId, userId in MDC for the logging.
     * In reactive systems we cannot affirm on a thread that is picked to execute the job, Since it is multi-threaded
     * and execute the request concurrently thread that runs doSubscribe might be differtent than
     * the thread the runs doOnError, since MDC is thread local we attach userId and traceId to MDC on each listener
     *
     * @param traceId request id
     * @param userId  user id
     * @param action
     */
    private void withMdc(String traceId, String userId, Runnable action) {
        try {
            if (traceId != null) MDC.put(TRACE_ID, traceId);
            if (userId != null) MDC.put(USER_ID, userId);

            action.run();

        } finally {
            MDC.remove(TRACE_ID);
            MDC.remove(USER_ID);
        }
    }
}