package com.stampede.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

    @Id
    private String tokenHash;

    private String personId;
    private Instant expiresAt;
    private Instant createdAt;

    protected EmailVerificationToken() {
    }

    public EmailVerificationToken(String tokenHash, String personId, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.personId = personId;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public String getTokenHash() { return tokenHash; }
    public String getPersonId() { return personId; }
}