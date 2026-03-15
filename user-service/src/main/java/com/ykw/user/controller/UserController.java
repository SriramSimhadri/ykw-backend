package com.ykw.user.controller;

import com.ykw.user.api.UsersApi;
import com.ykw.user.dto.UserRegisterRequest;
import com.ykw.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UsersApi {

    @Override
    public ResponseEntity<UserResponse> registerUser(UserRegisterRequest userRegisterRequest) {
        return null;
    }
}