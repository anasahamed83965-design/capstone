package com.college.babysitter.controller;

import com.college.babysitter.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public landing resource at {@code /} so opening the bare backend URL
 * shows API information instead of a bare 403 from the security chain.
 */
@RestController
public class RootController {

    /**
     * Describes the API and where to find health, docs and the frontend.
     *
     * @return public API landing payload
     */
    @GetMapping("/")
    public ApiResponse<Map<String, String>> index() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("name", "Babysitter Booking API");
        info.put("health", "/api/health");
        info.put("docs", "/swagger-ui.html");
        info.put("directory", "/api/babysitters");
        return ApiResponse.ok(info, "Babysitter Booking API is running");
    }
}
