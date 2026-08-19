package com.college.babysitter.service;

import com.college.babysitter.dto.PaymentDto;
import com.college.babysitter.dto.PaymentRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.AvailabilitySlot;
import com.college.babysitter.model.Babysitter;
import com.college.babysitter.model.Booking;
import com.college.babysitter.model.Payment;
import com.college.babysitter.model.PaymentStatus;
import com.college.babysitter.model.Role;
import com.college.babysitter.model.User;
import com.college.babysitter.repository.BookingRepository;
import com.college.babysitter.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    private User parent;
    private Booking booking;

    @BeforeEach
    void setUp() {
        parent = new User("Maria Lopez", "maria@example.com", "hashed", "555-0100", Role.PARENT);
        parent.setId(1L);
        User sitterUser = new User("Ana Torres", "ana@example.com", "hashed", "555-0200", Role.BABYSITTER);
        sitterUser.setId(2L);
        Babysitter babysitter = new Babysitter(sitterUser, "Friendly", 3, new BigDecimal("20.00"));
        babysitter.setId(5L);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        AvailabilitySlot slot = new AvailabilitySlot(babysitter, start, start.plusHours(2));
        slot.setId(7L);
        booking = new Booking(parent, babysitter, slot, start, start.plusHours(2),
                new BigDecimal("40.00"), null);
        booking.setId(30L);
    }

    @Test
    void create_recordsPendingPaymentWithDefaultMethod() {
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(30L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(11L);
            return p;
        });

        PaymentDto dto = paymentService.create(1L, request(30L, null));

        assertEquals(PaymentStatus.PENDING, dto.getStatus());
        assertEquals("CASH", dto.getMethod());
        assertEquals(new BigDecimal("40.00"), dto.getAmount());
    }

    @Test
    void create_duplicate_throws() {
        Payment existing = new Payment(booking, new BigDecimal("40.00"), "CASH");
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(30L)).thenReturn(Optional.of(existing));

        assertThrows(ApiException.class, () -> paymentService.create(1L, request(30L, "UPI")));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void create_nonParent_throwsForbidden() {
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));

        assertThrows(ApiException.class, () -> paymentService.create(99L, request(30L, "CASH")));
    }

    @Test
    void create_missingBooking_throwsNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.create(1L, request(99L, "CASH")));
    }

    @Test
    void markPaid_settlesPendingPayment() {
        Payment payment = new Payment(booking, new BigDecimal("40.00"), "UPI");
        payment.setId(11L);
        when(paymentRepository.findById(11L)).thenReturn(Optional.of(payment));

        PaymentDto dto = paymentService.markPaid(1L, 11L);

        assertEquals(PaymentStatus.PAID, dto.getStatus());
        assertNotNull(dto.getPaidAt());
        assertNotNull(dto.getTransactionRef());
        assertTrue(dto.getTransactionRef().startsWith("TXN-"));
    }

    @Test
    void markPaid_alreadySettled_throws() {
        Payment payment = new Payment(booking, new BigDecimal("40.00"), "UPI");
        payment.setId(11L);
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findById(11L)).thenReturn(Optional.of(payment));

        assertThrows(ApiException.class, () -> paymentService.markPaid(1L, 11L));
    }

    private PaymentRequest request(Long bookingId, String method) {
        PaymentRequest req = new PaymentRequest();
        req.setBookingId(bookingId);
        req.setMethod(method);
        return req;
    }
}
