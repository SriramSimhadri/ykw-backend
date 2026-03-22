package com.ykw.auth.security;

import com.ykw.auth.dto.CurrentUser;
import com.ykw.auth.error.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration}")
    private Duration expiration;

    @Value("${spring.application.name}")
    private String serviceName;


    public String generateAccessToken(Long userId, String email, String role, String status) {
        Instant now = Instant.now();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(serviceName)
                .claim("email", email)
                .claim("role", role)
                .claim("status", status)
                .claim("userId", userId)
                .claim("jti", UUID.randomUUID().toString()) // to uniquely identify the token
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public CurrentUser getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new UnauthorizedException("Invalid or missing authentication");
        }

        Jwt jwt = jwtAuth.getToken();

        String jti = jwt.getId();
        if (jti == null) {
            throw new UnauthorizedException("Invalid token: missing jti");
        }

        var expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            throw new UnauthorizedException("Invalid token: missing expiration");
        }

        var claim = jwt.getClaim("userId");
        if (claim == null) {
            throw new UnauthorizedException("Invalid token: missing userId");
        }

        Long userId = ((Number) claim).longValue();

        return new CurrentUser(userId, jti, expiresAt);
    }
}