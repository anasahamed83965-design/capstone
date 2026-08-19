package com.college.babysitter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The single response envelope used by every endpoint:
 * {@code {success, data, message}}. Clients only need one shape to parse.
 *
 * @param <T> payload type
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

    /**
     * Builds a success envelope with the default message.
     *
     * @param data payload
     * @param <T> payload type
     * @return success envelope
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "ok");
    }

    /**
     * Builds a success envelope with a custom message.
     *
     * @param data payload
     * @param message human-readable message
     * @param <T> payload type
     * @return success envelope
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    /**
     * Builds an error envelope without payload.
     *
     * @param message human-readable error
     * @param <T> payload type
     * @return error envelope
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }

    /**
     * Builds an error envelope carrying details (e.g. field errors).
     *
     * @param message human-readable error
     * @param data error details
     * @param <T> details type
     * @return error envelope
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, data, message);
    }
}
