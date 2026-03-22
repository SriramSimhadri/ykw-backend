package com.ykw.auth.dto;

import java.time.Instant;

public record CurrentUser(
        Long userId,
        String jti,
        Instant expiresAt
) {}