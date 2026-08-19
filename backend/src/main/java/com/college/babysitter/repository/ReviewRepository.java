package com.college.babysitter.repository;

import com.college.babysitter.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Review persistence. One booking has at most one review.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Finds the review for a booking (used to block duplicates).
     *
     * @param bookingId booking id
     * @return the review, if present
     */
    Optional<Review> findByBookingId(Long bookingId);
}
