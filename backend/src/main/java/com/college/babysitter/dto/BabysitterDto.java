package com.college.babysitter.dto;

import com.college.babysitter.model.Babysitter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Public sitter view: profile fields plus the account name/phone and the
 * live average rating. Built from the entity so JPA details never leak.
 */
@Data
@AllArgsConstructor
public class BabysitterDto {

    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String bio;
    private int experienceYears;
    private BigDecimal hourlyRate;
    private boolean verified;
    private double avgRating;

    /**
     * Converts a sitter entity (with its account loaded) to the public view.
     *
     * @param b sitter entity
     * @return the public view
     */
    public static BabysitterDto from(Babysitter b) {
        return new BabysitterDto(
                b.getId(),
                b.getUser().getId(),
                b.getUser().getFullName(),
                b.getUser().getPhone(),
                b.getBio(),
                b.getExperienceYears(),
                b.getHourlyRate(),
                b.isVerified(),
                b.getAvgRating());
    }
}
