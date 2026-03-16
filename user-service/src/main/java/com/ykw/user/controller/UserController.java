package com.ykw.user.controller;

import com.ykw.user.api.UsersApi;
import com.ykw.user.dto.UserRegisterRequest;
import com.ykw.user.dto.UserResponse;
import com.ykw.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handles the user profile specific actions
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    /**
     * Holds UserService instance
     */
    private final UserService userService;

    /**
     *
     * @param xTraceId Unique trace identifier used for request tracing across services (required)
     * @param userRegisterRequest  (required)
     * @return
     */
    @Override
    public ResponseEntity<UserResponse> registerUser(final String xTraceId, final UserRegisterRequest userRegisterRequest) {
        log.debug("method={}", "registerUser");
        userService.registerUser(userRegisterRequest);

        return null;
    }
}