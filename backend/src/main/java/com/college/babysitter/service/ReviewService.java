package com.college.babysitter.service;

import com.college.babysitter.dto.ReviewDto;
import com.college.babysitter.dto.ReviewRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.Babysitter;
import com.college.babysitter.model.Booking;
import com.college.babysitter.model.BookingStatus;
import com.college.babysitter.model.Review;
import com.college.babysitter.repository.BabysitterRepository;
import com.college.babysitter.repository.BookingRepository;
import com.college.babysitter.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Review logic: one review per completed booking, with immediate
 * recomputation of the sitter's average rating.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final BabysitterRepository babysitterRepository;

    /**
     * Wires the review collaborators (constructor injection keeps the class unit-testable).
     *
     * @param reviewRepository review persistence
     * @param bookingRepository booking lookup and ownership checks
     * @param babysitterRepository rating persistence
     */
    public ReviewService(ReviewRepository reviewRepository,
                         BookingRepository bookingRepository,
                         BabysitterRepository babysitterRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.babysitterRepository = babysitterRepository;
    }

    /**
     * Adds a review for a completed booking. Only the booking parent may
     * review, only once, and only after completion.
     *
     * @param reviewerId account id of the reviewer
     * @param bookingId completed booking id
     * @param request validated rating and comment
     * @return the saved review
     * @throws ApiException with 403 for non-parents, 400 for bad state or duplicates
     */
    @Transactional
    public ReviewDto addReview(Long reviewerId, Long bookingId, ReviewRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getParent().getId().equals(reviewerId)) {
            throw ApiException.forbidden("Only the parent who booked can review");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw ApiException.badRequest("You can only review a completed booking");
        }
        if (reviewRepository.findByBookingId(bookingId).isPresent()) {
            throw ApiException.badRequest("This booking has already been reviewed");
        }

        Review review = reviewRepository.save(
                new Review(booking, booking.getParent(), request.getRating(), request.getComment()));

        recomputeRating(booking.getBabysitter());
        return ReviewDto.from(review);
    }

    /**
     * Lists all reviews left for a sitter.
     *
     * @param babysitterId sitter id
     * @return the sitter's reviews
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> listForBabysitter(Long babysitterId) {
        return reviewRepository.findAll().stream()
                .filter(r -> r.getBooking().getBabysitter().getId().equals(babysitterId))
                .map(ReviewDto::from)
                .toList();
    }

    private void recomputeRating(Babysitter babysitter) {
        List<Review> reviews = reviewRepository.findAll().stream()
                .filter(r -> r.getBooking().getBabysitter().getId().equals(babysitter.getId()))
                .toList();
        double avg = reviews.isEmpty() ? 0.0
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        babysitter.setAvgRating(avg);
        babysitterRepository.save(babysitter);
    }
}
