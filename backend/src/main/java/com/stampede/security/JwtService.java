package com.stampede.security;

import com.stampede.model.Person;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;

/**
 * Creates and verifies JWT access tokens using RS256.
 * The private key signs; the public key verifies.
 */
@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessTokenExpiryMs;

    public JwtService(KeyPair jwtKeyPair,
                      @Value("${jwt.access-token-expiry-ms}") long accessTokenExpiryMs) {
        this.privateKey = jwtKeyPair.getPrivate();
        this.publicKey = jwtKeyPair.getPublic();
        this.accessTokenExpiryMs = accessTokenExpiryMs;
    }

    public String generateAccessToken(Person person) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(person.getPersonId())
                .claim("email", person.getEmail())
                .claim("role", person.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiryMs)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /** Returns the personId (subject) if the token is valid, else null. */
    public String extractPersonId(String token) {
        try {
            return parse(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /** Returns the role claim if the token is valid, else null. */
    public String extractRole(String token) {
        try {
            return parse(token).get("role", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
