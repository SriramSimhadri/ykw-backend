package com.ykw.article.service;

import com.ykw.common.keys.RedisKeys;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import com.ykw.common.utility.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    public void cacheArticle(Long userId, String header) {
        String key = RedisKeys.articleIdempotentKey(userId);
        redisTemplate.opsForValue().set(key, header);
    }
}

