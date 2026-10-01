package com.stampede.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "persons")
public class Person {

    @Id
    private String personId;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // never serialized to JSON — the hash must never leave the server
    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean emailVerified;

    private Instant createdAt;

    protected Person() {
        // default constructor for JPA
    }

    public Person(String personId, String name, String email, String passwordHash, Role role) {
        this.personId = personId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.emailVerified = false;
        this.createdAt = Instant.now();
    }

    public String getPersonId()     { return personId; }
    public String getName()         { return name; }
    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole()           { return role; }
    public Instant getCreatedAt()   { return createdAt; }
    public boolean isEmailVerified() { return emailVerified; }

    public void markEmailVerified() { this.emailVerified = true; }
}
