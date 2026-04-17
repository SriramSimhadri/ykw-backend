package com.ykw.common.keys;

import static com.ykw.common.constants.RedisConstants.*;

public final class RedisKeys {

    private RedisKeys() {}

    public static String authTokenBlackListKey(String hashedJti) {
        return  AUTH_BLACK_LIST_KEY + hashedJti;
    }

    public static String userRolesKey(String userId) {
        return  USER_ROLE_KEY + userId;
    }

    public static String articleIdempotencyKey(Long userId, String idempotencyKey) {
        return ARTICLE_IDEMPOTENCY + userId + ":" + idempotencyKey;
    }

    public static String articleMetadata(Long userId) {
        return ARTICLE_METADATA + userId;
    }
}