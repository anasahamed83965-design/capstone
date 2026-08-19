package com.college.babysitter.dto;

import com.college.babysitter.model.Payment;
import com.college.babysitter.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment record view: amount, method, status and sandbox references.
 */
@Data
@AllArgsConstructor
public class PaymentDto {

    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String method;
    private PaymentStatus status;
    private String transactionRef;
    private LocalDateTime paidAt;

    /**
     * Converts a payment entity (with its booking loaded) to the view.
     *
     * @param payment payment entity
     * @return the payment view
     */
    public static PaymentDto from(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getTransactionRef(),
                payment.getPaidAt());
    }
}
