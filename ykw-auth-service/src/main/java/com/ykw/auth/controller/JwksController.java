package com.ykw.auth.controller;

import com.ykw.auth.security.RsaKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwksController {

    private final RsaKeyProvider rsaKeyProvider;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getKeys() {
        return rsaKeyProvider.getJwkSet().toJSONObject();
    }
}