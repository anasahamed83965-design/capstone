package com.college.babysitter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Payment form: which booking to pay for, plus an optional method label.
 */
@Data
public class PaymentRequest {

    @NotNull(message = "Booking id is required")
    private Long bookingId;

    private String method;
}
