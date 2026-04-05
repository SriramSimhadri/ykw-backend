package com.ykw.api.gateway.filter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static com.ykw.common.constants.Constants.*;

/**
 * Handles the mapping of retrieved userId, traceId and user roles from the
 * security context. Map the fields to the request header, can be accessible by the
 * downstream services
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserContextFilter implements WebFilter {

    private final Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        Span span = tracer.spanBuilder("user.context").startSpan();

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
                .map(auth -> {

                    String userId = auth.getToken().getSubject();
                    String roles = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));

                    return new UserContext(userId, roles);
                })
                .defaultIfEmpty(new UserContext("anonymous", "ROLE_ANONYMOUS"))
                .flatMap(userContext -> {
                    try (Scope scope = span.makeCurrent()) {

                        String traceId = Span.current().getSpanContext().getTraceId();

                        span.setAttribute("user.id", userContext.userId());
                        span.setAttribute("user.roles", userContext.roles());

                        log.info("UserContext extracted traceId={}, userId={}, roles={}",
                                traceId, userContext.userId(), userContext.roles());

                        ServerHttpRequest mutatedRequest = exchange.getRequest()
                                .mutate()
                                .header(USER_HEADER, userContext.userId())
                                .header(ROLE_HEADER, userContext.roles())
                                .build();

                        return chain
                                .filter(exchange.mutate().request(mutatedRequest).build())
                                .contextWrite(ctx -> ctx
                                        .put(USER_ID, userContext.userId())
                                );
                    }
                }).doFinally(signal -> span.end());
    }

    private record UserContext(String userId, String roles) {}

}