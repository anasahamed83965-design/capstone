package com.college.babysitter.repository;

import com.college.babysitter.model.Babysitter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Sitter-profile persistence, including the filtered public directory query.
 */
public interface BabysitterRepository extends JpaRepository<Babysitter, Long> {

    /**
     * Finds the profile belonging to an account (at most one).
     *
     * @param userId account id
     * @return the profile, if present
     */
    Optional<Babysitter> findByUserId(Long userId);

    /**
     * Lists verified sitters, best rated first.
     *
     * @return verified sitters
     */
    List<Babysitter> findByVerifiedTrueOrderByAvgRatingDesc();

    /**
     * Public directory search: verified sitters above a rating floor and,
     * optionally, under a rate ceiling, best rated first.
     *
     * @param minRating minimum average rating
     * @param maxRate maximum hourly rate, or null for no ceiling
     * @return matching verified sitters
     */
    @Query("SELECT b FROM Babysitter b WHERE b.verified = true " +
           "AND b.avgRating >= :minRating " +
           "AND (:maxRate IS NULL OR b.hourlyRate <= :maxRate) " +
           "ORDER BY b.avgRating DESC")
    List<Babysitter> searchDirectory(@Param("minRating") double minRating,
                                     @Param("maxRate") BigDecimal maxRate);
}
