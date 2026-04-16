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

    public static String articleIdempotentKey(Long userId, String header) {
        return ARTICLE_IDEMPOTENCY_KEY + userId + ":" + header;
    }
}