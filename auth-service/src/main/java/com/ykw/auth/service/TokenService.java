package com.ykw.auth.service;

import java.time.Instant;

public interface TokenService {

    void blacklistToken(String jti, Instant expiresAt);

    String generateAccessToken(Long id, String email, String role, String status);
}
