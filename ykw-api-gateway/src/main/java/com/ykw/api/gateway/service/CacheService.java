package com.ykw.api.gateway.service;

import com.ykw.common.keys.RedisKeys;
import com.ykw.common.utility.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final ReactiveStringRedisTemplate redisTemplate;

    public Mono<Boolean> isTokenRevoked(String jti) {
        String hashedKey = HashUtil.hash(jti);
        return redisTemplate.hasKey(RedisKeys.authTokenBlackListKey(hashedKey));
    }

    public Mono<String> getUserRole(String userId) {
        return redisTemplate.opsForValue().get(RedisKeys.userRolesKey(userId));
    }
}