package com.ykw.common.keys;

public final class RedisKeys {

    private RedisKeys() {}

    public static String blacklistKey(String hashedJti) {
        return "blacklist:jti:" + hashedJti;
    }
}