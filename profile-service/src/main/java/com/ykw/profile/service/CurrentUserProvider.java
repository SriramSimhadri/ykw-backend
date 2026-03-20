package com.ykw.profile.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authentication found");
        }

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("Authentication is not JWT-based");
        }

        Jwt jwt = jwtAuth.getToken();

        Object claim = jwt.getClaim("userId");

        if (claim == null) {
            throw new IllegalStateException("userId claim missing");
        }

        return ((Number) claim).longValue();
    }
}