package com.college.babysitter.exception;

import org.springframework.http.HttpStatus;

/**
 * 404 error for lookups by id (users, sitters, slots, bookings, payments).
 * Unverified sitter profiles reuse it so they stay invisible to parents.
 */
public class ResourceNotFoundException extends ApiException {

    /**
     * Creates a 404 error.
     *
     * @param message client-facing error message
     */
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
