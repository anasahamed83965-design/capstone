package com.college.babysitter.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Application error carrying an HTTP status. Services throw this for expected
 * failures (bad input, wrong party, missing rows); the global handler turns
 * it into the standard {@code {success, data, message}} error envelope.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Creates an API error.
     *
     * @param status HTTP status to respond with
     * @param message client-facing error message
     */
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Creates a 400 Bad Request error.
     *
     * @param message client-facing error message
     * @return the exception
     */
    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Creates a 401 Unauthorized error.
     *
     * @param message client-facing error message
     * @return the exception
     */
    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * Creates a 403 Forbidden error.
     *
     * @param message client-facing error message
     * @return the exception
     */
    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }
}
