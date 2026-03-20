package com.ykw.auth.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Getter
@Component
public class RsaKeyProvider {

    private final RSAPrivateKey privateKey;

    private final RSAPublicKey publicKey;

    private final JWKSet jwkSet;

    public RsaKeyProvider(
            @Value("${jwt.private-key}") Resource privateKeyResource,
            @Value("${jwt.public-key}") Resource publicKeyResource) throws Exception {

        try (InputStream privateKeyStream = privateKeyResource.getInputStream();
             InputStream publicKeyStream = publicKeyResource.getInputStream()) {

            this.privateKey = RsaKeyConverters.pkcs8().convert(privateKeyStream);
            this.publicKey = RsaKeyConverters.x509().convert(publicKeyStream);

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("auth-key")
                    .algorithm(JWSAlgorithm.RS256)
                    .build();

            this.jwkSet = new JWKSet(rsaKey);
        }
    }
}