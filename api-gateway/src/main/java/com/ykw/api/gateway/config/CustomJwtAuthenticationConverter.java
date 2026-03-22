package com.ykw.api.gateway.config;

import com.ykw.api.gateway.service.TokenBlacklistService;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final TokenBlacklistService blacklistService;

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
                        return Mono.just(buildAuthentication(jwt));
                    });
        });
    }

    /**
     * Checks whether the token is blacklisted.
     * Fail-open strategy: if Redis fails, allow request but log error.
     */
    private Mono<Boolean> checkBlacklist(String jti) {
        return blacklistService.isBlacklisted(jti)
                .doOnError(e ->
                        log.error("Redis error during blacklist check, jti={}", jti, e))
                .onErrorResume(e -> Mono.just(false)); // fail-open
    }

    /**
     * Builds authentication object from JWT.
     */
    private AbstractAuthenticationToken buildAuthentication(Jwt jwt) {

        // Optionally extract authorities/roles here
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extract roles/authorities from JWT.
     * Customize based on your token structure.
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

        // Example: "roles": ["USER", "ADMIN"]
        String role = jwt.getClaim("role");

        if (role == null || role.isEmpty()) {
            return Collections.emptyList();
        }

        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}