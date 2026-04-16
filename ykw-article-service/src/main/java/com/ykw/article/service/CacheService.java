package com.ykw.article.service;

import com.ykw.common.keys.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCK = "LOCK";

    public boolean acquireLock(Long userId, String idempotencyKey) {
        String key = RedisKeys.articleIdempotentKey(userId, idempotencyKey);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, LOCK, 10, TimeUnit.SECONDS);

        return Boolean.TRUE.equals(success);
    }

    public void saveResult(Long userId, String idempotencyKey, String articleId) {
        String key = RedisKeys.articleIdempotentKey(userId, idempotencyKey);

        redisTemplate.opsForValue()
                .set(key, articleId, 15, TimeUnit.MINUTES);
    }

    public String getValue(Long userId, String idempotencyKey) {
        String key = RedisKeys.articleIdempotentKey(userId, idempotencyKey);
        return redisTemplate.opsForValue().get(key);
    }

    public boolean isLock(String value) {
        return LOCK.equals(value);
    }
}

