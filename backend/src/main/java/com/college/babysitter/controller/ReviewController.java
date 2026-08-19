package com.college.babysitter.controller;

import com.college.babysitter.dto.ApiResponse;
import com.college.babysitter.dto.ReviewDto;
import com.college.babysitter.dto.ReviewRequest;
import com.college.babysitter.security.AppUserPrincipal;
import com.college.babysitter.security.SecurityUtils;
import com.college.babysitter.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Review endpoints, nested under bookings because a review always belongs
 * to exactly one completed booking (which is also what blocks duplicates).
 */
@RestController
@RequestMapping("/api/bookings/{bookingId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Adds the parent's review for a completed booking and refreshes the
     * sitter's average rating.
     *
     * @param authentication the parent's JWT authentication
     * @param bookingId id of the completed booking
     * @param request validated rating and comment
     * @return the saved review
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewDto> add(Authentication authentication,
                                      @PathVariable Long bookingId,
                                      @Valid @RequestBody ReviewRequest request) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(reviewService.addReview(user.getId(), bookingId, request), "Review submitted");
    }
}
