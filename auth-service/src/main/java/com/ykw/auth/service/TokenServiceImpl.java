package com.ykw.auth.service;

import com.ykw.auth.security.JwtProvider;
import com.ykw.common.keys.RedisKeys;
import com.ykw.common.utility.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtProvider jwtProvider;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.expiration}")
    private Duration expiration;

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public void blacklistToken(String jti, Instant expiresAt) {

        String hashedJti = HashUtil.hash(jti);

        Duration ttl = Duration.between(Instant.now(), expiresAt);

        if (ttl.isNegative() || ttl.isZero()) return;

        String key = RedisKeys.blacklistKey(hashedJti);

        redisTemplate.opsForValue().set(key, "revoked", ttl);
    }

    @Override
    public String generateAccessToken(Long id, String email, String role, String status) {
        return jwtProvider.generateAccessToken(id, email, role, status);
    }
}