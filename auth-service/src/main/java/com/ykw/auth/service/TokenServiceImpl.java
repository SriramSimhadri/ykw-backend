package com.ykw.auth.service;

import com.ykw.auth.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtProvider jwtProvider;

    private final CacheService cacheService;

    @Value("${jwt.expiration}")
    private Duration expiration;

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public void blacklistToken(String jti, Instant expiresAt) {

        Duration ttl = Duration.between(Instant.now(), expiresAt);

        if (ttl.isNegative() || ttl.isZero()) return;

        cacheService.cacheRevokedToken(jti, ttl);
    }

    @Override
    public String generateAccessToken(Long id, String email, String role, String status) {
        return jwtProvider.generateAccessToken(id, email, role, status);
    }
}