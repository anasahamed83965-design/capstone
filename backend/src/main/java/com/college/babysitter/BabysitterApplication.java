package com.college.babysitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Babysitter Booking Platform API.
 * Bootstraps the Spring context (web, JPA, security, mail, OpenAPI).
 */
@SpringBootApplication
public class BabysitterApplication {

    /**
     * Starts the application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(BabysitterApplication.class, args);
    }
}
