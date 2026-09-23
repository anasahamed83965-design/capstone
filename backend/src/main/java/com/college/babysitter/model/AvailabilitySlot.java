package com.college.babysitter.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * One bookable time window of a sitter. The {@code booked} flag is the
 * single lock that prevents double-booking: it flips inside the same
 * transaction that creates the booking.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "availability_slots")
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "babysitter_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Babysitter babysitter;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean booked;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Creates an open slot. New slots start unbooked.
     *
     * @param babysitter owning sitter
     * @param startTime slot start (must be in the future)
     * @param endTime slot end (must be after the start)
     */
    public AvailabilitySlot(Babysitter babysitter, LocalDateTime startTime, LocalDateTime endTime) {
        this.babysitter = babysitter;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
