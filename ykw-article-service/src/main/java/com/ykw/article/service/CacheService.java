package com.ykw.article.service;

import com.ykw.common.keys.RedisKeys;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCK = "LOCK";

    @CircuitBreaker(name = "redis", fallbackMethod = "getValueFallback")
    public String getValue(Long authorId, String idempotencyKey) {
        String key = RedisKeys.articleIdempotencyKey(authorId, idempotencyKey);
        return redisTemplate.opsForValue().get(key);
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "acquireLockFallback")
    public boolean acquireLock(Long authorId, String idempotencyKey) {
        String key = RedisKeys.articleIdempotencyKey(authorId, idempotencyKey);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, LOCK, 1, TimeUnit.MINUTES);

        return Boolean.TRUE.equals(success);
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "saveIdempotencyFallback")
    public void saveIdempotency(Long authorId, String idempotencyKey, String articleId) {
        String key = RedisKeys.articleIdempotencyKey(authorId, idempotencyKey);
        redisTemplate.opsForValue()
                .set(key, articleId, 1, TimeUnit.MINUTES);
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "saveArticleMetadataFallback")
    public void saveArticleMetadata(Long authorId,
                            String slug) {
        String articleSlugKey = RedisKeys.articleMetadata(authorId);
        redisTemplate.opsForZSet().add(articleSlugKey, slug, System.currentTimeMillis());
    }

    public Set<String> getArticles(Long authorId) {
        String key = RedisKeys.articleMetadata(authorId);
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    @CircuitBreaker(name = "redis", fallbackMethod = "evictFallback")
    public void evictArticle(Long authorId, String slug) {
        String key = RedisKeys.articleMetadata(authorId);
        redisTemplate.opsForZSet().remove(key, slug);
    }

    public boolean isLock(String value) {
        return LOCK.equals(value);
    }

    public String getValueFallback(Long authorId, String idempotencyKey, Exception ex) {
        return null;
    }

    public boolean acquireLockFallback(Long authorId, String idempotencyKey, Exception ex) {
        return false;
    }

    public void saveIdempotencyFallback(Long authorId, String idempotencyKey, String articleId, Exception ex) {
    }

    public void saveArticleMetadataFallback(Long authorId, String slug, Exception ex) {
    }


    public void evictFallback(Long authorId, String slug, Exception ex) {
    }
}