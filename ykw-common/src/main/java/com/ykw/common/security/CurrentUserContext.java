package com.ykw.common.security;

import com.ykw.common.dto.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Optional;

import static com.ykw.common.constants.Constants.USER_ID;
import static com.ykw.common.constants.Constants.USER_ROLE;

public class CurrentUserContext {

    public CurrentUser getCurrentUser() {
        return extractCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Missing or invalid authentication"));
    }

    public Optional<CurrentUser> getCurrentUserOptional() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return Optional.empty();
        }

        Jwt jwt = jwtAuth.getToken();

        return buildUserFromJwt(jwt);
    }

    private Optional<CurrentUser> extractCurrentUser() {
        return getCurrentUserOptional();
    }

    private Optional<CurrentUser> buildUserFromJwt(Jwt jwt) {

        String jti = jwt.getId();
        Instant expiresAt = jwt.getExpiresAt();

        Object userIdClaim = jwt.getClaim(USER_ID);
        Object roleClaim = jwt.getClaim(USER_ROLE);

        if (jti == null || expiresAt == null) {
            return Optional.empty();
        }

        if (!(userIdClaim instanceof Number number)) {
            return Optional.empty();
        }

        Long userId = number.longValue();
        String role = roleClaim != null ? roleClaim.toString() : "UNKNOWN";

        return Optional.of(new CurrentUser(userId, jti, expiresAt, role));
    }
}