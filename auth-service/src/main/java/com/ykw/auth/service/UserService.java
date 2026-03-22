package com.ykw.auth.service;

import com.ykw.auth.dto.AuthResponse;
import com.ykw.auth.dto.LoginRequest;
import com.ykw.auth.dto.UserRegisterRequest;

public interface UserService {

    AuthResponse registerUser(final UserRegisterRequest request);

    AuthResponse loginUser(final LoginRequest loginRequest, final String traceId);

    void logout();
}
