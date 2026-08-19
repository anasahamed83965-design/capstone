package com.college.babysitter.controller;

import com.college.babysitter.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Liveness probe for basic monitoring (Review-II section 9.6).
 * Public on purpose so load balancers, hosting platforms and mentors
 * can check the service without logging in.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Returns the service status and current server time.
     *
     * @return an {@code OK} status payload wrapped in the standard API envelope
     */
    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "OK", "time", LocalDateTime.now().toString()));
    }
}
