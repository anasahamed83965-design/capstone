package com.college.babysitter.controller;

import com.college.babysitter.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.college.babysitter.dto.AuthResponse;
import com.college.babysitter.dto.LoginRequest;
import com.college.babysitter.dto.SignupRequest;
import com.college.babysitter.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints. Kept thin on purpose: input validation
 * happens here via {@code @Valid}, all account logic lives in
 * {@link com.college.babysitter.service.AuthService}.
 */
@Tag(name = "Auth", description = "Signup, login and admin seeding (public)")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a parent or babysitter account and returns a JWT immediately,
     * so the user lands logged in without a second login call.
     *
     * @param request validated signup payload
     * @return the new account plus its JWT
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request), "Account created");
    }

    /**
     * Authenticates with email and password.
     *
     * @param request validated login payload
     * @return the account plus a fresh JWT
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request), "Logged in");
    }

    /**
     * Creates the platform admin account on first call; later calls simply
     * return the existing admin so setup scripts stay idempotent.
     *
     * @return the admin account plus its JWT
     */
    @PostMapping("/seed-admin")
    public ApiResponse<AuthResponse> seedAdmin() {
        return ApiResponse.ok(authService.seedAdmin(), "Admin account ready");
    }
}
