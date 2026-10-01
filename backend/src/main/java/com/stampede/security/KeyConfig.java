package com.stampede.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.security.KeyPair;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * Provides the RSA key pair used to sign (private key) and verify (public key)
 * JWT access tokens with RS256.
 *
 * <p>For local development, a fresh key pair is generated when no configured
 * private key is present. Cloud Run supplies the shared private key from Secret
 * Manager so every instance derives the same RSA key pair.
 */
@Configuration
public class KeyConfig {

    private final String encodedPrivateKey;

    public KeyConfig(@Value("${jwt.private-key:}") String encodedPrivateKey) {
        this.encodedPrivateKey = encodedPrivateKey;
    }

    @Bean
    public KeyPair jwtKeyPair() {
        if (!encodedPrivateKey.isBlank()) {
            return keyPairFromPrivateKey(encodedPrivateKey);
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA not available", e);
        }
    }

    private KeyPair keyPairFromPrivateKey(String encodedKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
                RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;
                return new KeyPair(
                    keyFactory.generatePublic(new RSAPublicKeySpec(
                        rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent())),
                    privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT private key", e);
        }
    }
}
