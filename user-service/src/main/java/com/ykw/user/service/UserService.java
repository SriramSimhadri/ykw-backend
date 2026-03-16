package com.ykw.user.service;

import com.ykw.user.dto.UserRegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    public void registerUser(UserRegisterRequest userRegisterRequest) {
        log.info("User name {}", userRegisterRequest.getName());
    }
}
