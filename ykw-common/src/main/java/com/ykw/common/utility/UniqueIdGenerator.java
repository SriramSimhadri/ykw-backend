package com.ykw.common.utility;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * Utility for generating ULID(Universally Unique Lexicographically Sortable Identifier)
 */
public class UniqueIdGenerator {

    public static String generate() {
        return UlidCreator.getMonotonicUlid().toLowerCase();
    }
}
