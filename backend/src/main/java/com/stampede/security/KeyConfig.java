package com.stampede.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Provides the RSA key pair used to sign (private key) and verify (public key)
 * JWT access tokens with RS256.
 *
 * <p>NOTE: this generates a fresh key pair on every startup. That is fine for a
 * single instance — access tokens are short-lived and refresh tokens are opaque
 * and stored in the DB, so a restart only forces clients to refresh once. For a
 * multi-instance production deployment the key pair must be shared across
 * instances: load it from a secret manager / KMS or mounted PEM files instead of
 * generating it here.
 */
@Configuration
public class KeyConfig {

    @Bean
    public KeyPair jwtKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA not available", e);
        }
    }
}
