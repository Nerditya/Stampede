package com.stampede.service;

import com.stampede.model.EmailVerificationToken;
import com.stampede.model.Person;
import com.stampede.repository.EmailVerificationTokenRepository;
import com.stampede.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final PersonRepository personRepository;
    private final ResendEmailService resendEmailService;
    private final String frontendUrl;
    private final Duration tokenLifetime;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository,
                                    PersonRepository personRepository,
                                    ResendEmailService resendEmailService,
                                    @Value("${app.frontend-url}") String frontendUrl,
                                    @Value("${app.email-verification-token-lifetime:PT30M}") Duration tokenLifetime) {
        this.tokenRepository = tokenRepository;
        this.personRepository = personRepository;
        this.resendEmailService = resendEmailService;
        this.frontendUrl = frontendUrl;
        this.tokenLifetime = tokenLifetime;
    }

    @Transactional
    public void sendVerificationEmail(Person person) {
        tokenRepository.deleteByPersonId(person.getPersonId());
        String rawToken = randomToken();
        tokenRepository.save(new EmailVerificationToken(
                hash(rawToken), person.getPersonId(), Instant.now().plus(tokenLifetime)));
        resendEmailService.sendVerificationEmail(
                person.getEmail(), frontendUrl + "/verify-email?token=" + rawToken);
    }

    @Transactional
    public void verify(String rawToken) {
        EmailVerificationToken token = tokenRepository.findById(hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired verification link"));
        if (token.isExpired()) {
            tokenRepository.delete(token);
            throw new BadCredentialsException("Invalid or expired verification link");
        }
        Person person = personRepository.findById(token.getPersonId())
                .orElseThrow(() -> new BadCredentialsException("Account no longer exists"));
        person.markEmailVerified();
        tokenRepository.delete(token);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}