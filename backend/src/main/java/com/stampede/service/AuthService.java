package com.stampede.service;

import com.stampede.dto.AuthResponse;
import com.stampede.dto.LoginRequest;
import com.stampede.dto.RegisterRequest;
import com.stampede.dto.RegistrationResponse;
import com.stampede.exception.EmailAlreadyExistsException;
import com.stampede.model.Person;
import com.stampede.model.RefreshToken;
import com.stampede.model.Role;
import com.stampede.repository.PersonRepository;
import com.stampede.repository.RefreshTokenRepository;
import com.stampede.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final PersonRepository personRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final long refreshTokenExpiryMs;

    public AuthService(PersonRepository personRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailVerificationService emailVerificationService,
                       @Value("${jwt.refresh-token-expiry-ms}") long refreshTokenExpiryMs) {
        this.personRepository = personRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailVerificationService = emailVerificationService;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (personRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }
        Person person = new Person(
                "USR-" + UUID.randomUUID(),
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER);
        personRepository.save(person);
            emailVerificationService.sendVerificationEmail(person);
            return new RegistrationResponse("Check your email to verify your account");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Person person = personRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), person.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!person.isEmailVerified()) {
            throw new BadCredentialsException("Please verify your email before logging in");
        }
        return issueTokens(person);
    }

    @Transactional
    public void verifyEmail(String token) {
        emailVerificationService.verify(token);
    }

    /**
     * Exchanges a valid refresh token for a new access token, rotating the refresh
     * token (delete the old, issue a new) — a standard defense against token replay.
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new BadCredentialsException("Refresh token expired");
        }

        Person person = personRepository.findById(stored.getPersonId())
                .orElseThrow(() -> new BadCredentialsException("Account no longer exists"));

        refreshTokenRepository.delete(stored);   // rotate
        return issueTokens(person);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteById(refreshToken);
    }

    private AuthResponse issueTokens(Person person) {
        String accessToken = jwtService.generateAccessToken(person);
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(new RefreshToken(
                refreshToken,
                person.getPersonId(),
                Instant.now().plusMillis(refreshTokenExpiryMs)));
        return new AuthResponse(accessToken, refreshToken);
    }
}
