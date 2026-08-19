package com.college.babysitter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Booking form: which sitter, which open slot, and optional notes for the sitter.
 */
@Data
public class BookingRequest {

    @NotNull(message = "Babysitter id is required")
    private Long babysitterId;

    @NotNull(message = "Slot id is required")
    private Long slotId;

    @Size(max = 500, message = "Notes are too long")
    private String notes;
}
