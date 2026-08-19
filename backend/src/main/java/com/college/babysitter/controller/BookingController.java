package com.college.babysitter.controller;

import com.college.babysitter.dto.ApiResponse;
import com.college.babysitter.dto.BookingDto;
import com.college.babysitter.dto.BookingRequest;
import com.college.babysitter.dto.StatusRequest;
import com.college.babysitter.security.AppUserPrincipal;
import com.college.babysitter.security.SecurityUtils;
import com.college.babysitter.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Booking engine endpoints. Parents create bookings; both sides move them
 * through PENDING -&gt; CONFIRMED -&gt; COMPLETED/CANCELLED with role checks
 * enforced in {@link com.college.babysitter.service.BookingService}.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Books an open slot with a verified sitter. The slot is claimed inside
     * the same transaction so two parents can never book it twice.
     *
     * @param authentication the parent's JWT authentication
     * @param request validated booking payload (sitter id, slot id, notes)
     * @return the pending booking with its computed total
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingDto> create(Authentication authentication,
                                          @Valid @RequestBody BookingRequest request) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(bookingService.createBooking(user.getId(), request), "Booking created");
    }

    /**
     * Lists the logged-in user's bookings (own bookings for parents,
     * incoming bookings for sitters).
     *
     * @param authentication the caller's JWT authentication
     * @return bookings newest first
     */
    @GetMapping
    public ApiResponse<List<BookingDto>> myBookings(Authentication authentication) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(bookingService.listForUser(user.getId(), user.getRole()));
    }

    /**
     * Moves a booking to CONFIRMED, COMPLETED or CANCELLED. Only valid
     * transitions by the right party succeed; anything else is rejected.
     *
     * @param authentication the actor's JWT authentication
     * @param id booking id
     * @param request validated target status
     * @return the updated booking
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<BookingDto> updateStatus(Authentication authentication,
                                                @PathVariable Long id,
                                                @Valid @RequestBody StatusRequest request) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(bookingService.updateStatus(user.getId(), user.getRole(), id, request.getStatus()),
                "Booking updated");
    }
}
