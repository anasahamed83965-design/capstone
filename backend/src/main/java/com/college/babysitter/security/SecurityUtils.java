package com.college.babysitter.security;

import com.college.babysitter.exception.ApiException;
import org.springframework.security.core.Authentication;

/**
 * Helper for controllers to unwrap the logged-in account. Centralized here
 * so every endpoint fails the same way (401) when the token is missing.
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Extracts the account principal from a Spring authentication.
     *
     * @param authentication the request authentication, may be null
     * @return the logged-in account principal
     * @throws ApiException with 401 when nobody is logged in
     */
    public static AppUserPrincipal currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw ApiException.unauthorized("Please log in");
        }
        return principal;
    }
}
