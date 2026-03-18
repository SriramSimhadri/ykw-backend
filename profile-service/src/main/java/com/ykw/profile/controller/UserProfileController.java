package com.ykw.profile.controller;

import com.ykw.profile.api.UsersApi;
import com.ykw.profile.dto.AuthResponse;
import com.ykw.profile.dto.UserRegisterRequest;
import com.ykw.profile.service.JwtService;
import com.ykw.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handles the user profile specific actions
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserProfileController implements UsersApi {

    /**
     * Holds UserService instance
     */
    private final UserProfileService userService;

    private final JwtService jwtService;

    /**
     *
     * @param xTraceId Unique trace identifier used for request tracing across services (required)
     * @param userRegisterRequest  (required)
     * @return
     */
    @Override
    public ResponseEntity<AuthResponse> registerUser(final String xTraceId, final UserRegisterRequest userRegisterRequest) {
        log.debug("method={}", "registerUser");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.registerUser(userRegisterRequest));
    }
}