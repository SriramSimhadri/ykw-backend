package com.ykw.auth.service;

import com.ykw.auth.dto.AuthResponse;
import com.ykw.auth.dto.UserRegisterRequest;
import com.ykw.auth.mapper.UserMapper;
import com.ykw.auth.model.User;
import com.ykw.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public AuthResponse registerUser(final UserRegisterRequest request) {
        log.debug("method {}", "registerUser");

        User user = createUser(request);

        String token = jwtService.generateAccessToken(user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name());

        return new AuthResponse()
                .accessToken(token)
                .user(userMapper.toResponse(user));
    }

    @Transactional
    private User createUser(UserRegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        return userRepository.save(user);
    }
}
