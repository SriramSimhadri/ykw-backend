package com.ykw.article.service;

import com.ykw.common.keys.RedisKeys;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCK = "LOCK";

    @CircuitBreaker(name = "redis", fallbackMethod = "getValueFallback")
    public String getValue(Long userId, String idempotencyKey) {
        String key = RedisKeys.articleIdempotentKey(userId, idempotencyKey);
        return redisTemplate.opsForValue().get(key);
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "acquireLockFallback")
    public boolean acquireLock(Long userId, String idempotencyKey) {
        String key = RedisKeys.articleIdempotentKey(userId, idempotencyKey);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, LOCK, 10, TimeUnit.SECONDS);

        return Boolean.TRUE.equals(success);
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "saveResultFallback")
    public void saveResult(Long userId, String idempotencyKey, String articleId) {
        String key = RedisKeys.articleIdempotentKey(userId, idempotencyKey);

        redisTemplate.opsForValue()
                .set(key, articleId, 15, TimeUnit.MINUTES);
    }

    public boolean isLock(String value) {
        return LOCK.equals(value);
    }

    public String getValueFallback(Long userId, String idempotencyKey, Exception ex) {
        return null;
    }

    public boolean acquireLockFallback(Long userId, String idempotencyKey, Exception ex) {
        return true;
    }

    public void saveResultFallback(Long userId, String idempotencyKey, String articleId, Exception ex) {
    }
}