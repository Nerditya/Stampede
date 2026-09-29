package com.stampede.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A long-lived, opaque token stored server-side so it can be revoked.
 * Used only to mint new short-lived access tokens.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    private String token;   // opaque random string (the primary key)

    private String personId;
    private Instant expiresAt;
    private Instant createdAt;

    protected RefreshToken() {
        // default constructor for JPA
    }

    public RefreshToken(String token, String personId, Instant expiresAt) {
        this.token = token;
        this.personId = personId;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public String getToken()      { return token; }
    public String getPersonId()   { return personId; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}
