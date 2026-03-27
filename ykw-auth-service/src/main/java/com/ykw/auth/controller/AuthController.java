package com.ykw.auth.controller;

import com.ykw.auth.api.AuthApi;
import com.ykw.auth.dto.AuthResponse;
import com.ykw.auth.dto.LoginRequest;
import com.ykw.auth.dto.UserRegisterRequest;
import com.ykw.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;

    @Override
    public ResponseEntity<AuthResponse> registerUser(final String xTraceId,
                                                     final UserRegisterRequest userRegisterRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.registerUser(userRegisterRequest));
    }

    @Override
    public ResponseEntity<AuthResponse> loginUser(final String xTraceId,
                                                     final LoginRequest loginRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.loginUser(loginRequest, xTraceId));
    }

    @Override
    public ResponseEntity<Void> logoutUser(String xTraceId) {
        userService.logout();
        return ResponseEntity.noContent().build();
    }
}
