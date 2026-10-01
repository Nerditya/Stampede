package com.stampede.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService {

    private final JavaMailSender mailSender;
    private final String username;

    public SmtpEmailService(JavaMailSender mailSender,
                            @Value("${spring.mail.username:}") String username) {
        this.mailSender = mailSender;
        this.username = username;
    }

    public void sendVerificationEmail(String recipient, String verificationUrl) {
        if (username.isBlank()) {
            throw new IllegalStateException("MAIL_USERNAME is not configured");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(username);
            helper.setTo(recipient);
            helper.setSubject("Verify your Stampede email");
            helper.setText(
                    "<p>Welcome to Stampede.</p>"
                            + "<p><a href=\"" + verificationUrl + "\">Verify your email</a></p>"
                            + "<p>This link expires in 30 minutes.</p>",
                    true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Could not send verification email", e);
        }
    }
}