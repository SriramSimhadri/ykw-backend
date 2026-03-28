package com.ykw.api.gateway.config;

import com.ykw.api.gateway.service.CacheService;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Custom auth converter that handles token validation, verifies userId presence in jwt.
 * and user role validation comparing with the redis
 */
@Component
@RequiredArgsConstructor
public class CustomAuthConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final CacheService cacheService;

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        return Mono.defer(() -> {

            String jti = jwt.getId();
            String userId = jwt.getSubject();

            if (jti == null || jti.isBlank()) {
                LogUtil.warn(
                        LogEvent.create("AUTH_INVALID_TOKEN").add("reason", "missing_jti")
                );
                return Mono.error(new BadCredentialsException("Invalid token"));
            }

            if (userId == null || userId.isBlank()) {
                LogUtil.warn(
                        LogEvent.create("AUTH_INVALID_TOKEN")
                                .add("reason", "missing_subject")
                );
                return Mono.error(new BadCredentialsException("Invalid token"));
            }

            // Blacklist check (fail-closed)
            return cacheService.isTokenRevoked(jti)

                    .onErrorResume(ex -> {
                        LogUtil.error(
                                LogEvent.create("AUTH_REDIS_FAILURE")
                                        .userId(userId)
                                        .add("jti", jti)
                                        .error(ex.getMessage())
                        );
                        return Mono.error(new AuthenticationServiceException("Auth service unavailable"));
                    })

                    .flatMap(isRevoked -> {
                        if (Boolean.TRUE.equals(isRevoked)) {
                            LogUtil.warn(
                                    LogEvent.create("AUTH_TOKEN_REVOKED")
                                            .userId(userId)
                                            .add("jti", jti)
                            );
                            return Mono.error(new BadCredentialsException("Token revoked"));
                        }

                        // Build authentication
                        return buildAuthentication(jwt, userId);
                    });
        });
    }

    private Mono<AbstractAuthenticationToken> buildAuthentication(Jwt jwt, String userId) {
        return cacheService.getUserRole(userId)
                .onErrorResume(ex -> {
                    LogUtil.error(
                            LogEvent.create("AUTH_ROLE_FETCH_FAILURE")
                                    .userId(userId)
                                    .error(ex.getMessage())
                    );
                    return Mono.error(new AuthenticationServiceException("Auth service unavailable"));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    LogUtil.warn(
                            LogEvent.create("AUTH_ROLE_MISSING")
                                    .userId(userId)
                    );

                    return Mono.error(new BadCredentialsException("No role assigned"));
                }))
                .map(role -> {
                    if (!List.of("USER", "ADMIN").contains(role)) {
                        LogUtil.warn(
                                LogEvent.create("AUTH_INVALID_ROLE")
                                        .userId(userId)
                                        .add("role", role)
                        );
                        throw new BadCredentialsException("Invalid role");
                    }
                    return new JwtAuthenticationToken(
                            jwt,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                });
    }
}