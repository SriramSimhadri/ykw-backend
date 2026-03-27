package com.ykw.security;

import com.ykw.common.dto.CurrentUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class CurrentUserContext {

    public CurrentUser getCurrentUser() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("Invalid or missing authentication");
        }

        Jwt jwt = jwtAuth.getToken();

        String jti = jwt.getId();
        var expiresAt = jwt.getExpiresAt();
        var userIdClaim = jwt.getClaim("userId");

        if (jti == null || expiresAt == null || userIdClaim == null) {
            throw new IllegalStateException("Invalid JWT claims");
        }

        Long userId = ((Number) userIdClaim).longValue();

        return new CurrentUser(userId, jti, expiresAt);
    }
}