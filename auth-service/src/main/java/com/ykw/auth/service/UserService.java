package com.ykw.auth.service;

import com.ykw.auth.dto.AuthResponse;
import com.ykw.auth.dto.LoginRequest;
import com.ykw.auth.dto.UserRegisterRequest;
import com.ykw.auth.error.EmailAlreadyExistsException;
import com.ykw.auth.mapper.UserMapper;
import com.ykw.auth.model.User;
import com.ykw.auth.model.UserStatus;
import com.ykw.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    /**
     * Handles new user registration
     * @param request user registration request object that contains user details
     * @return token + user unique information
     */
    public AuthResponse registerUser(final UserRegisterRequest request) {
        log("registerUser");

        User user = createUser(request);

        String token = jwtService.generateAccessToken(user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name());

        return new AuthResponse()
                .accessToken(token)
                .user(userMapper.toResponse(user));
    }

    /**
     * Handles user login request
     * <p>
     *    Verifies the password, user status and then generates the token
     * </p>
     * @param loginRequest login request contains user credentials
     * @return token + user unique information
     */
    public AuthResponse loginUser(final LoginRequest loginRequest, final String traceId) {
        log("loginUser");

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid Credentials");
        }

        if (UserStatus.ACTIVE != user.getStatus()) {
            throw new RuntimeException("Account disabled");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name()
        );

        return new AuthResponse()
                .accessToken(accessToken)
                .user(userMapper.toResponse(user));
    }

    @Transactional
    private User createUser(UserRegisterRequest request) {

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        try {
            return userRepository.save(user);
        } catch (final DataIntegrityViolationException ex) {

            if (ex.getMostSpecificCause().getMessage().contains("users_email_key")) {
                throw new EmailAlreadyExistsException("Email already exists");
            }

            throw ex;
        }
    }

    private void log(final String method) {
        log.debug("method {}", method);
    }
}
