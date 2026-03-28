package com.ykw.common.utility;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmConstraints;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashUtil {

    public static String hash(String jti) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(jti.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
