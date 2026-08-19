package com.college.babysitter.repository;

import com.college.babysitter.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Payment persistence. One booking has at most one payment row.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds the payment for a booking (used to block duplicates).
     *
     * @param bookingId booking id
     * @return the payment, if present
     */
    Optional<Payment> findByBookingId(Long bookingId);
}
