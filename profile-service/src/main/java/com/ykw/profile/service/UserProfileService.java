package com.ykw.profile.service;

import com.ykw.profile.dto.AuthResponse;
import com.ykw.profile.dto.UserRegisterRequest;
import com.ykw.profile.mapper.UserMapper;
import com.ykw.profile.model.User;
import com.ykw.profile.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    private final JwtService jwtService;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    private User createUser(UserRegisterRequest request) {

        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImageUrl(request.getProfileImageUrl().toString())
                .bio(request.getBio())
                .build();

        return userProfileRepository.save(user);
    }

    public AuthResponse registerUser(UserRegisterRequest request) {
        log.debug("method {}", "registerUser");
        Instant start = Instant.now();
        User user = createUser(request);
        Instant end = Instant.now();

        log.debug("Duration {}", Duration.between(start, end));
        String token = jwtService.generateAccessToken(user.getId().toString(), user.getEmail());

        return new AuthResponse()
                .accessToken(token)
                .user(userMapper.toResponse(user));

    }


}
