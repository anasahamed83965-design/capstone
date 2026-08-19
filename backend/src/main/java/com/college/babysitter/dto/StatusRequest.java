package com.college.babysitter.dto;

import com.college.babysitter.model.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Status-change form carrying the target booking status.
 */
@Data
public class StatusRequest {

    @NotNull(message = "Status is required")
    private BookingStatus status;
}
