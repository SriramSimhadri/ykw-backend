package com.ykw.auth.controller;

import com.ykw.auth.api.AuthApi;
import com.ykw.auth.dto.AuthResponse;
import com.ykw.auth.dto.LoginRequest;
import com.ykw.auth.dto.UserRegisterRequest;
import com.ykw.auth.service.UserService;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.ykw.common.constants.Constants.USER_EMAIL;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;

    @Override
    public ResponseEntity<AuthResponse> registerUser(final UserRegisterRequest request) {

        LogUtil.info(LogEvent.create("REGISTER_REQUEST").add(USER_EMAIL, request.getEmail()));

        var response = userService.registerUser(request);

        LogUtil.info(LogEvent.create("REGISTER_SUCCESS").userId(response.getUser().getId()));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Override
    public ResponseEntity<AuthResponse> loginUser(final LoginRequest request) {

        LogUtil.info(LogEvent.create("LOGIN_REQUEST").add(USER_EMAIL, request.getEmail()));

        var response = userService.loginUser(request);

        LogUtil.info(LogEvent.create("LOGIN_SUCCESS").userId(response.getUser().getId()));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }

    @Override
    public ResponseEntity<Void> logoutUser() {

        userService.logout();

        return ResponseEntity.noContent().build();
    }
}
