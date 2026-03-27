package com.ykw.api.gateway.filter.logging;

import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        String traceId = exchange.getAttribute("traceId");
        String userId = exchange.getAttribute("userId");

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange)

                // inject into the reactor context, subsequent chain of actions will use this
                .contextWrite(ctx -> {
                    if (traceId != null) ctx = ctx.put("traceId", traceId);
                    if (userId != null) ctx = ctx.put("userId", userId);
                    return ctx;
                })

                // bridge the reactor context with MDC
                .doOnEach(signal -> {
                    if (!signal.isOnComplete()) {
                        signal.getContextView().getOrEmpty("traceId")
                                .ifPresent(tid -> MDC.put("traceId", (String) tid));

                        signal.getContextView().getOrEmpty("userId")
                                .ifPresent(uid -> MDC.put("userId", (String) uid));
                    }
                })

                // request received
                .doOnSubscribe(sub -> {
                    LogUtil.info(
                            LogEvent.create("REQUEST_RECEIVED")
                                    .method(method)
                                    .path(path)
                    );
                })

                // on success
                .doOnSuccess(aVoid -> {
                    long latency = System.currentTimeMillis() - startTime;

                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 200;

                    LogUtil.info(
                            LogEvent.create("REQUEST_COMPLETED")
                                    .status(status)
                                    .latency(latency)
                    );
                })

                // on error
                .doOnError(error -> {
                    long latency = System.currentTimeMillis() - startTime;

                    LogUtil.error(
                            LogEvent.create("REQUEST_FAILED")
                                    .error(error.getMessage())
                                    .latency(latency)
                    );
                })

                // clean up the MDC
                .doFinally(signal -> MDC.clear());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}