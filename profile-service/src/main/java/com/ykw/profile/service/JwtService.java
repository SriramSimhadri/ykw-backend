package com.ykw.profile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateAccessToken(String userId, String email) {
        Instant now = Instant.now();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("ykw-user-service")
                .claim("email", email)
                .subject(userId)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiration))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
}