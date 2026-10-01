package com.stampede.repository;

import com.stampede.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {

    void deleteByPersonId(String personId);
}