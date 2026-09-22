package com.college.babysitter.controller;

import com.college.babysitter.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.college.babysitter.dto.PaymentDto;
import com.college.babysitter.dto.PaymentRequest;
import com.college.babysitter.security.AppUserPrincipal;
import com.college.babysitter.security.SecurityUtils;
import com.college.babysitter.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Sandbox payment endpoints. No real money moves: these calls only record
 * what is owed and what was settled, so both sides stay informed.
 */
@Tag(name = "Payments", description = "Sandbox payments: record what is owed, mark what is paid")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Records a pending sandbox payment for one of the parent's bookings.
     *
     * @param authentication the parent's JWT authentication
     * @param request validated payload (booking id, optional method)
     * @return the pending payment record
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentDto> create(Authentication authentication,
                                          @Valid @RequestBody PaymentRequest request) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(paymentService.create(user.getId(), request), "Payment recorded");
    }

    /**
     * Marks a pending payment as paid and stamps a sandbox transaction reference.
     *
     * @param authentication the parent's JWT authentication
     * @param id payment id
     * @return the settled payment record
     */
    @PostMapping("/{id}/pay")
    public ApiResponse<PaymentDto> markPaid(Authentication authentication, @PathVariable Long id) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(paymentService.markPaid(user.getId(), id), "Payment marked as paid");
    }
}
