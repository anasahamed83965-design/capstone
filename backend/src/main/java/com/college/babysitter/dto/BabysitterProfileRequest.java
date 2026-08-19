package com.college.babysitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Sitter profile form: bio, experience and hourly rate. Bean validation
 * rejects blanks and negative numbers before the service ever runs.
 */
@Data
public class BabysitterProfileRequest {

    @NotBlank(message = "Bio is required")
    private String bio;

    @PositiveOrZero(message = "Experience cannot be negative")
    private int experienceYears;

    @NotNull(message = "Hourly rate is required")
    @PositiveOrZero(message = "Hourly rate cannot be negative")
    private BigDecimal hourlyRate;
}
