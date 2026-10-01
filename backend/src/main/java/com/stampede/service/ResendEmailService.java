package com.stampede.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class ResendEmailService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String from;

    public ResendEmailService(ObjectMapper objectMapper,
                              @Value("${resend.api-key:}") String apiKey,
                              @Value("${resend.from:onboarding@resend.dev}") String from) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.from = from;
    }

    public void sendVerificationEmail(String recipient, String verificationUrl) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY is not configured");
        }
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "from", from,
                    "to", new String[]{recipient},
                    "subject", "Verify your Stampede email",
                    "html", "<p>Welcome to Stampede.</p>"
                            + "<p><a href=\"" + verificationUrl + "\">Verify your email</a></p>"
                            + "<p>This link expires in 30 minutes.</p>"));
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Resend rejected email: HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not send verification email", e);
        }
    }
}