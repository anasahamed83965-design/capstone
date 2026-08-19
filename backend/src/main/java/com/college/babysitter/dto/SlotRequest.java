package com.college.babysitter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Slot creation form: start and end time. Range and overlap rules are
 * enforced in the service, not just here.
 */
@Data
public class SlotRequest {

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}
