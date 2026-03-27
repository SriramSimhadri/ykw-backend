package com.ykw.api.gateway.config;

import com.ykw.api.gateway.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJwtAuthConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final CacheService redisUserService;

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {

        return Mono.defer(() -> {
            String jti = jwt.getId();

            if (jti == null || jti.isBlank()) {
                log.warn("JWT rejected: missing jti");
                return Mono.error(new BadCredentialsException("Invalid token"));
            }

            return checkBlacklist(jti)
                    .flatMap(isBlacklisted -> {
                        if (Boolean.TRUE.equals(isBlacklisted)) {
                            log.warn("JWT rejected: token is blacklisted, jti={}", jti);
                            return Mono.error(new BadCredentialsException("Token revoked"));
                        }
                        return buildAuthentication(jwt);
                    });
        });
    }

    /**
     * Checks whether the token is blacklisted.
     */
    private Mono<Boolean> checkBlacklist(String jti) {
        return redisUserService.isTokenRevoked(jti)
                .doOnError(e ->
                        log.error("Redis error during blacklist check, jti={}", jti, e))
                .onErrorResume(e ->
                        Mono.error(new BadCredentialsException("Token validation failed")));
    }

    private Mono<AbstractAuthenticationToken> buildAuthentication(Jwt jwt) {

        String userId = jwt.getSubject();

        return redisUserService.getUserRole(userId)
                .switchIfEmpty(Mono.error(new BadCredentialsException("No role assigned")))
                .map(role -> {
                    if (!List.of("USER", "ADMIN").contains(role)) {
                        throw new BadCredentialsException("Invalid role");
                    }
                    Collection<GrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    return new JwtAuthenticationToken(jwt, authorities);
                });
    }
}