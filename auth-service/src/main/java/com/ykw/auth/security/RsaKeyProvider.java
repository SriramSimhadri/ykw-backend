package com.ykw.auth.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

@Getter
@Component
public class RsaKeyProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RsaKeyProvider(
            @Value("${jwt.private-key}") Resource privateKeyResource,
            @Value("${jwt.public-key}") Resource publicKeyResource) throws Exception {

        try (InputStream privateKeyStream = privateKeyResource.getInputStream();
             InputStream publicKeyStream = publicKeyResource.getInputStream()) {

            this.privateKey = RsaKeyConverters.pkcs8().convert(privateKeyStream);
            this.publicKey = RsaKeyConverters.x509().convert(publicKeyStream);
        }
    }

}