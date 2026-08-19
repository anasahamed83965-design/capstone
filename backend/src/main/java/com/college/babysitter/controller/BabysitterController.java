package com.college.babysitter.controller;

import com.college.babysitter.dto.ApiResponse;
import com.college.babysitter.dto.BabysitterDto;
import com.college.babysitter.dto.BabysitterProfileRequest;
import com.college.babysitter.dto.SlotDto;
import com.college.babysitter.dto.SlotRequest;
import com.college.babysitter.security.AppUserPrincipal;
import com.college.babysitter.security.SecurityUtils;
import com.college.babysitter.service.AvailabilityService;
import com.college.babysitter.service.BabysitterService;
import com.college.babysitter.service.ReviewService;
import com.college.babysitter.dto.ReviewDto;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Babysitter directory (public) plus the logged-in sitter's own area
 * (profile and availability slots). Read endpoints stay public so parents
 * can browse without an account; writes require a babysitter JWT.
 */
@RestController
@RequestMapping("/api/babysitters")
public class BabysitterController {

    private final BabysitterService babysitterService;
    private final AvailabilityService availabilityService;
    private final ReviewService reviewService;

    public BabysitterController(BabysitterService babysitterService,
                                AvailabilityService availabilityService,
                                ReviewService reviewService) {
        this.babysitterService = babysitterService;
        this.availabilityService = availabilityService;
        this.reviewService = reviewService;
    }

    // --- public directory ---

    /**
     * Lists verified sitters, optionally filtered by minimum rating and maximum rate.
     *
     * @param minRating minimum average rating, or null for no floor
     * @param maxRate maximum hourly rate, or null for no ceiling
     * @return verified sitters ordered by rating
     */
    @GetMapping
    public ApiResponse<List<BabysitterDto>> list(@RequestParam(required = false) Double minRating,
                                                 @RequestParam(required = false) BigDecimal maxRate) {
        return ApiResponse.ok(babysitterService.listVerified(minRating, maxRate));
    }

    /**
     * Returns one verified sitter's public profile.
     *
     * @param id babysitter id
     * @return the public profile
     */
    @GetMapping("/{id}")
    public ApiResponse<BabysitterDto> get(@PathVariable Long id) {
        return ApiResponse.ok(babysitterService.getPublicProfile(id));
    }

    /**
     * Lists a sitter's future, not-yet-booked slots for the booking form.
     *
     * @param id babysitter id
     * @return open slots ordered by start time
     */
    @GetMapping("/{id}/slots")
    public ApiResponse<List<SlotDto>> openSlots(@PathVariable Long id) {
        return ApiResponse.ok(availabilityService.listOpenSlots(id));
    }

    /**
     * Lists all reviews left for a sitter.
     *
     * @param id babysitter id
     * @return the sitter's reviews
     */
    @GetMapping("/{id}/reviews")
    public ApiResponse<List<ReviewDto>> reviews(@PathVariable Long id) {
        return ApiResponse.ok(reviewService.listForBabysitter(id));
    }

    // --- babysitter's own area ---

    /**
     * Returns the logged-in sitter's own profile, including unverified state.
     *
     * @param authentication the sitter's JWT authentication
     * @return the sitter's profile
     */
    @GetMapping("/me")
    public ApiResponse<BabysitterDto> myProfile(Authentication authentication) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(BabysitterDto.from(babysitterService.getMyProfile(user.getId())));
    }

    /**
     * Creates or updates the logged-in sitter's profile (bio, experience, rate).
     * Editing never auto-verifies: the admin must still approve the profile.
     *
     * @param authentication the sitter's JWT authentication
     * @param request validated profile payload
     * @return the saved profile
     */
    @PutMapping("/me")
    public ApiResponse<BabysitterDto> updateProfile(Authentication authentication,
                                                    @Valid @RequestBody BabysitterProfileRequest request) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(babysitterService.createOrUpdateProfile(user.getId(), request), "Profile saved");
    }

    /**
     * Lists all of the logged-in sitter's slots, booked and open.
     *
     * @param authentication the sitter's JWT authentication
     * @return the sitter's slots ordered by start time
     */
    @GetMapping("/me/slots")
    public ApiResponse<List<SlotDto>> mySlots(Authentication authentication) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(availabilityService.listMySlots(user.getId()));
    }

    /**
     * Adds an availability slot. Past and overlapping slots are rejected so
     * the calendar (and therefore bookings) stays consistent.
     *
     * @param authentication the sitter's JWT authentication
     * @param request validated slot payload
     * @return the created slot
     */
    @PostMapping("/me/slots")
    public ApiResponse<SlotDto> addSlot(Authentication authentication,
                                        @Valid @RequestBody SlotRequest request) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(availabilityService.addSlot(user.getId(), request), "Slot added");
    }

    /**
     * Deletes one of the logged-in sitter's slots. Booked slots are protected
     * because a booking already depends on them.
     *
     * @param authentication the sitter's JWT authentication
     * @param slotId id of the slot to delete
     * @return empty success envelope
     */
    @DeleteMapping("/me/slots/{slotId}")
    public ApiResponse<Void> deleteSlot(Authentication authentication, @PathVariable Long slotId) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        availabilityService.deleteSlot(user.getId(), slotId);
        return ApiResponse.ok(null, "Slot deleted");
    }
}
