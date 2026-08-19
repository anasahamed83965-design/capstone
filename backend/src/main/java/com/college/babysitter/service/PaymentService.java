package com.college.babysitter.service;

import com.college.babysitter.dto.PaymentDto;
import com.college.babysitter.dto.PaymentRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.Booking;
import com.college.babysitter.model.Payment;
import com.college.babysitter.model.PaymentStatus;
import com.college.babysitter.repository.BookingRepository;
import com.college.babysitter.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sandbox payment records. No real money moves; the service only tracks what
 * is owed per booking and marks it settled with a fake transaction reference.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    /**
     * Wires the payment collaborators (constructor injection keeps the class unit-testable).
     *
     * @param paymentRepository payment persistence
     * @param bookingRepository booking lookup
     */
    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Sandbox only - no real money moves. Creates a pending payment record
     * for a booking, so both sides can see the amount that is owed.
     */
    /**
     * Records a pending payment for a booking. Only the booking parent may
     * pay, and only once per booking.
     *
     * @param userId payer account id
     * @param request booking id plus optional method (defaults to CASH)
     * @return the pending payment record
     * @throws ApiException with 403 for non-parents, 400 for duplicates
     */
    @Transactional
    public PaymentDto create(Long userId, PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getParent().getId().equals(userId)) {
            throw ApiException.forbidden("Only the parent can pay for this booking");
        }
        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw ApiException.badRequest("Payment already exists for this booking");
        }

        String method = request.getMethod() == null ? "CASH" : request.getMethod();
        Payment payment = paymentRepository.save(new Payment(booking, booking.getTotalAmount(), method));
        return PaymentDto.from(payment);
    }

    /**
     * Settles a pending payment, stamping payment time and a sandbox
     * transaction reference. Settled payments cannot be paid twice.
     *
     * @param userId payer account id
     * @param paymentId payment id
     * @return the settled payment record
     * @throws ApiException with 403 for non-parents, 400 if already settled
     */
    @Transactional
    public PaymentDto markPaid(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getBooking().getParent().getId().equals(userId)) {
            throw ApiException.forbidden("Only the parent can settle this payment");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw ApiException.badRequest("This payment is already settled");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return PaymentDto.from(payment);
    }
}
