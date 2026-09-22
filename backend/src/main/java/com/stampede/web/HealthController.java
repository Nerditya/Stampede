package com.stampede.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A tiny endpoint that proves the server is alive and answering requests.
 *
 * <p>{@code @RestController} tells Spring this class handles web requests and that
 * whatever a method returns should be serialized straight to the HTTP response
 * body (as JSON here, since we return a Map). {@code @RequestMapping("/api")}
 * prefixes every route in this class, so the method below answers GET /api/health.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "stampede-backend",
                "time", Instant.now().toString()
        );
    }
}
