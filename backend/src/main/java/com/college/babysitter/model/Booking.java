package com.college.babysitter.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A parent's booking of a sitter for one slot. Times and total are snapshotted
 * onto the row so history survives even if the slot is later edited.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id")
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "babysitter_id")
    private Babysitter babysitter;

    // slot is snapshotted so history survives even if the slot is later edited
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", unique = true)
    private AvailabilitySlot slot;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Creates a booking. New bookings always start PENDING; confirmation is
     * a separate, permission-checked step.
     *
     * @param parent booking parent
     * @param babysitter booked sitter
     * @param slot claimed slot (marked booked by the caller)
     * @param startTime snapshot of the slot start
     * @param endTime snapshot of the slot end
     * @param totalAmount hourly rate times hours, rounded to cents
     * @param notes optional parent notes for the sitter
     */
    public Booking(User parent, Babysitter babysitter, AvailabilitySlot slot,
                   LocalDateTime startTime, LocalDateTime endTime, BigDecimal totalAmount, String notes) {
        this.parent = parent;
        this.babysitter = babysitter;
        this.slot = slot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAmount = totalAmount;
        this.notes = notes;
    }
}
