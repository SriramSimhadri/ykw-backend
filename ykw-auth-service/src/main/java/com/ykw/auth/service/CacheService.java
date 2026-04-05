package com.ykw.auth.service;

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

    public void cacheRevokedToken(String jti, Duration ttl) {

        String key = RedisKeys.authTokenBlackListKey(HashUtil.hash(jti));
        redisTemplate.opsForValue().set(key, "revoked", ttl);

        LogUtil.debug(LogEvent.create("CACHE_TOKEN_REVOKED")
                        .add("key", key)
                        .add("ttl", ttl.getSeconds())
        );
    }

    public void cacheUserRole(Long userId, String role) {
        String key = RedisKeys.userRolesKey(String.valueOf(userId));
        redisTemplate.opsForValue().set(key, role);
    }
}

