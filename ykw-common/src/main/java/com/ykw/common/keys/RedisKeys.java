package com.ykw.common.keys;

public final class RedisKeys {

    private RedisKeys() {}

    public static String authTokenBlackListKey(String hashedJti) {
        return "auth:blacklist:jti:" + hashedJti;
    }

    public static String userRolesKey(String userId) {
        return "user:role:" + userId;
    }
}