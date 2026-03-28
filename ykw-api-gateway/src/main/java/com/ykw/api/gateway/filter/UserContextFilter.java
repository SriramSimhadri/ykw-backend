package com.ykw.api.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Collectors;

import static com.ykw.common.constants.Constants.*;

/**
 * Handles the mapping of retrieved userId, traceId and user roles from the
 * security context. Map the fields to the request header, can be accessible by the
 * downstream services
 */
@Component
@Slf4j
public class UserContextFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        log.info("UserContextFilter triggered");

        String traceId = exchange.getAttribute(TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        final String finalTraceId = traceId;

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .map(auth -> {
                    log.info("Authentication found: {}", auth);

                    String userId = auth.getToken().getSubject();
                    String roles = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));

                    return new UserContext(userId, roles);
                })
                .defaultIfEmpty(new UserContext("anonymous", "ROLE_ANONYMOUS"))
                .doOnNext(uc -> {
                    if ("anonymous".equals(uc.userId)) {
                        log.warn("No authentication found, using anonymous user");
                    }
                })
                .flatMap(userContext -> {

                    log.info("Forwarding headers traceId={}, userId={}, role={}",
                            finalTraceId, userContext.userId, userContext.roles);

                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header(TRACE_HEADER, finalTraceId)
                            .header(USER_HEADER, userContext.userId)
                            .header(ROLE_HEADER, userContext.roles)
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build())
                            .contextWrite(ctx -> ctx
                                    .put(USER_ID, userContext.userId())
                                    .put(TRACE_ID, finalTraceId)
                            );
                });
    }

    private record UserContext(String userId, String roles) {}
}