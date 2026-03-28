package com.ykw.common.keys;

import static com.ykw.common.constants.RedisConstants.AUTH_BLACK_LIST_KEY;
import static com.ykw.common.constants.RedisConstants.USER_ROLE_KEY;

public final class RedisKeys {

    private RedisKeys() {}

    public static String authTokenBlackListKey(String hashedJti) {
        return  AUTH_BLACK_LIST_KEY + hashedJti;
    }

    public static String userRolesKey(String userId) {
        return  USER_ROLE_KEY+ userId;
    }
}