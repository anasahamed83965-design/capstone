package com.college.babysitter.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A parent's rating of one completed booking (one-to-one). Reviews feed the
 * sitter's average rating, recomputed on every new review.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Creates a review. Callers enforce: completed booking, booking parent
     * as reviewer, one review per booking, rating 1-5.
     *
     * @param booking the reviewed booking
     * @param reviewer the booking parent
     * @param rating stars from 1 to 5
     * @param comment optional comment
     */
    public Review(Booking booking, User reviewer, int rating, String comment) {
        this.booking = booking;
        this.reviewer = reviewer;
        this.rating = rating;
        this.comment = comment;
    }
}
