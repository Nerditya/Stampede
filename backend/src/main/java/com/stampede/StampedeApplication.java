package com.stampede;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Stampede flash-sale backend.
 *
 * <p>{@code @SpringBootApplication} bundles three things: it marks this as a
 * configuration class, turns on Spring Boot's auto-configuration (which wires up
 * an embedded Tomcat web server, JSON handling, etc.), and tells Spring to scan
 * this package and its sub-packages for components like our controllers.
 */
@SpringBootApplication
public class StampedeApplication {

    public static void main(String[] args) {
        SpringApplication.run(StampedeApplication.class, args);
    }
}
