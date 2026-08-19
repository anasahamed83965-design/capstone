package com.college.babysitter.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sandbox payment record for one booking (one-to-one). Tracks what is owed
 * and whether it was settled; no real money moves.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 50)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 100)
    private String transactionRef;

    private LocalDateTime paidAt;

    /**
     * Creates a payment record. New payments start PENDING.
     *
     * @param booking the paid-for booking
     * @param amount amount owed (copied from the booking total)
     * @param method payment method label, e.g. CASH
     */
    public Payment(Booking booking, BigDecimal amount, String method) {
        this.booking = booking;
        this.amount = amount;
        this.method = method;
    }
}
