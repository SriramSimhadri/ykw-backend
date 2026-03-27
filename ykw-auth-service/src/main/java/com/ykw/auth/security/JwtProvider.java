package com.ykw.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
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
}