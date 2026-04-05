package com.ykw.auth.service;

import com.ykw.auth.dto.AuthResponse;
import com.ykw.auth.dto.LoginRequest;
import com.ykw.auth.dto.UserRegisterRequest;
import com.ykw.auth.error.EmailAlreadyExistsException;
import com.ykw.auth.mapper.UserMapper;
import com.ykw.auth.model.User;
import com.ykw.auth.model.UserStatus;
import com.ykw.auth.repository.UserRepository;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import com.ykw.common.security.CurrentUserContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.ykw.common.constants.Constants.USER_EMAIL;
import static com.ykw.common.constants.Constants.USER_ROLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final CurrentUserContext currentUserContext;

    private final CacheService cacheService;

    /**
     * Handles new user registration
     * @param request user registration request object that contains user details
     * @return token + user unique information
     */
    @Override
    public AuthResponse registerUser(final UserRegisterRequest request) {

        LogUtil.info(LogEvent.create("USER_CREATE_INIT").add(USER_EMAIL, request.getEmail()));

        User user = createUser(request);

        String token = tokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name(), user.getStatus().name());

        cacheService.cacheUserRole(user.getId(), user.getRole().name());

        LogUtil.info(LogEvent.create("USER_CREATED").userId(user.getId()).add(USER_ROLE, user.getRole().name()));

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
    public AuthResponse loginUser(final LoginRequest loginRequest) {

        LogUtil.info(LogEvent.create("LOGIN_PROCESSING").add("email", loginRequest.getEmail()));

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    LogUtil.warn(LogEvent.create("LOGIN_FAILED_USER_NOT_FOUND")
                            .add("email", loginRequest.getEmail())
                    );
                    return new RuntimeException("Invalid Credentials");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {

            LogUtil.warn(LogEvent.create("LOGIN_FAILED_INVALID_PASSWORD")
                            .userId(user.getId())
            );
            throw new RuntimeException("Invalid Credentials");
        }

        if (UserStatus.ACTIVE != user.getStatus()) {
            LogUtil.warn(
                    LogEvent.create("LOGIN_FAILED_INACTIVE_USER")
                            .userId(user.getId())
            );
            throw new RuntimeException("Account disabled");
        }


        String accessToken = tokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name(), user.getStatus().name());

        cacheService.cacheUserRole(user.getId(), user.getRole().name());

        LogUtil.info(LogEvent.create("LOGIN_SUCCESS")
                        .userId(user.getId())
        );

        return new AuthResponse()
                .accessToken(accessToken)
                .user(userMapper.toResponse(user));
    }

    /**
     * Handles the user logout request, add the current token to the blacklist token list
     */
    @Override
    public void logout() {

        var user = currentUserContext.getCurrentUser();

        LogUtil.info(LogEvent.create("LOGOUT_INIT")
                .userId(user.userId())
                .add("jti", user.jti())
        );

        tokenService.blacklistToken(user.jti(), user.expiresAt());

        LogUtil.info(
                LogEvent.create("LOGOUT_SUCCESS")
                        .userId(user.userId())
        );
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
}
