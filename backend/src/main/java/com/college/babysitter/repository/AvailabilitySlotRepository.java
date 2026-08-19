package com.college.babysitter.repository;

import com.college.babysitter.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Slot persistence for sitter calendars and the booking form.
 */
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    /**
     * Lists all of a sitter's slots in calendar order.
     *
     * @param babysitterId sitter id
     * @return the sitter's slots ordered by start time
     */
    List<AvailabilitySlot> findByBabysitterIdOrderByStartTimeAsc(Long babysitterId);

    /**
     * Lists a sitter's future unbooked slots (what parents can pick).
     *
     * @param babysitterId sitter id
     * @param now cutoff; slots starting after this count as bookable
     * @return open slots ordered by start time
     */
    List<AvailabilitySlot> findByBabysitterIdAndBookedFalseAndStartTimeAfterOrderByStartTimeAsc(
            Long babysitterId, java.time.LocalDateTime now);
}
