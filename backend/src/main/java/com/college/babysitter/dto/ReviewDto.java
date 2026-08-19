package com.college.babysitter.dto;

import com.college.babysitter.model.Review;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Review view: stars, comment and reviewer name for sitter profiles.
 */
@Data
@AllArgsConstructor
public class ReviewDto {

    private Long id;
    private Long bookingId;
    private String reviewerName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    /**
     * Converts a review entity (with booking and reviewer loaded) to the view.
     *
     * @param review review entity
     * @return the review view
     */
    public static ReviewDto from(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getBooking().getId(),
                review.getReviewer().getFullName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt());
    }
}
