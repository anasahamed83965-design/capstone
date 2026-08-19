package com.college.babysitter.repository;

import com.college.babysitter.model.Booking;
import com.college.babysitter.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Booking persistence for parent histories, sitter inboxes and admin oversight.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Lists a parent's bookings, newest first.
     *
     * @param parentId parent account id
     * @return the parent's bookings
     */
    List<Booking> findByParentIdOrderByCreatedAtDesc(Long parentId);

    /**
     * Lists a sitter's incoming bookings, newest first.
     *
     * @param babysitterId sitter id
     * @return the sitter's bookings
     */
    List<Booking> findByBabysitterIdOrderByCreatedAtDesc(Long babysitterId);

    /**
     * Counts a sitter's bookings in one status.
     *
     * @param babysitterId sitter id
     * @param status status to count
     * @return matching booking count
     */
    long countByBabysitterIdAndStatus(Long babysitterId, BookingStatus status);
}
