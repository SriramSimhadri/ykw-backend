package com.ykw.api.gateway.filter;

import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
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
public class LoggingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        String traceId = exchange.getAttribute(TRACE_ID);
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange)
                .contextWrite(ctx -> {
                    if (traceId != null) {
                        return ctx.put(TRACE_ID, traceId);
                    }
                    return ctx;
                })
                //on each request, here no userId yet since ths the global filter
                .doOnSubscribe(sub -> {
                    withMdc(traceId, "anonymous", () ->
                            LogUtil.info(
                                    LogEvent.create("REQUEST_RECEIVED")
                                            .traceId(traceId)
                                            .method(method)
                                            .path(path)
                            )
                    );
                })
                //on each request success
                .doOnSuccess(aVoid ->
                        Mono.deferContextual(ctx -> {

                            String userId = ctx.getOrDefault(USER_ID, "anonymous");
                            int status = exchange.getResponse().getStatusCode() != null
                                    ? exchange.getResponse().getStatusCode().value()
                                    : 200;
                            long latency = System.currentTimeMillis() - startTime;
                            withMdc(traceId, userId, () ->
                                    LogUtil.info(
                                            LogEvent.create("REQUEST_COMPLETED")
                                                    .traceId(traceId)
                                                    .userId(userId)
                                                    .status(status)
                                                    .latency(latency)
                                    )
                            );
                            return Mono.empty();
                        }).subscribe()
                )
                //on error
                .doOnError(error ->
                        Mono.deferContextual(ctx -> {
                            String userId = ctx.getOrDefault(USER_ID, "anonymous");
                            long latency = System.currentTimeMillis() - startTime;
                            withMdc(traceId, userId, () ->
                                    LogUtil.error(
                                            LogEvent.create("REQUEST_FAILED")
                                                    .traceId(traceId)
                                                    .userId(userId)
                                                    .error(error.getMessage())
                                                    .latency(latency)
                                    )
                            );
                            return Mono.empty();
                        }).subscribe()
                );
    }

    /**
     * Wrap traceId, userId in MDC for the logging.
     * In reactive systems we cannot affirm on a thread that is picked to execute the job, Since it is multi-threaded
     * and execute the request concurrently thread that runs doSubscribe might be differtent than
     * the thread the runs doOnError, since MDC is thread local we attach userId and traceId to MDC on each listener
     * @param traceId request id
     * @param userId user id
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