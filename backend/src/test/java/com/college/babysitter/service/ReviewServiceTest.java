package com.college.babysitter.service;

import com.college.babysitter.dto.ReviewRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.model.*;
import com.college.babysitter.repository.BabysitterRepository;
import com.college.babysitter.repository.BookingRepository;
import com.college.babysitter.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BabysitterRepository babysitterRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User parent;
    private Babysitter babysitter;
    private Booking completedBooking;

    @BeforeEach
    void setUp() {
        parent = new User("Maria Lopez", "maria@example.com", "hashed", "555-0100", Role.PARENT);
        parent.setId(1L);
        babysitter = new Babysitter(
                new User("Ana Torres", "ana@example.com", "hashed", "555-0200", Role.BABYSITTER),
                "Friendly", 3, new BigDecimal("20.00"));
        babysitter.setId(5L);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        AvailabilitySlot slot = new AvailabilitySlot(babysitter, start, start.plusHours(2));
        slot.setId(7L);
        completedBooking = new Booking(parent, babysitter, slot, start, start.plusHours(2),
                new BigDecimal("40.00"), null);
        completedBooking.setId(30L);
        completedBooking.setStatus(BookingStatus.COMPLETED);
    }

    @Test
    void addReview_succeedsForParentOfCompletedBooking() {
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(completedBooking));
        when(reviewRepository.findByBookingId(30L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(9L);
            return r;
        });
        when(reviewRepository.findAll()).thenReturn(List.of());

        var dto = reviewService.addReview(1L, 30L, request(5, "Great babysitter"));

        assertEquals(5, dto.getRating());
        assertEquals(9L, dto.getId());
        assertEquals(0.0, babysitter.getAvgRating());
        verify(babysitterRepository, times(1)).save(babysitter);
    }

    @Test
    void addReview_recomputesAverageRating() {
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(completedBooking));
        when(reviewRepository.findByBookingId(30L)).thenReturn(Optional.empty());

        Review saved = new Review(completedBooking, parent, 4, "Very good");
        saved.setId(9L);
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        Review older = new Review(completedBooking, parent, 2, "Okay");
        older.setId(8L);
        when(reviewRepository.findAll()).thenReturn(List.of(saved, older));

        reviewService.addReview(1L, 30L, request(4, "Very good"));

        assertEquals(3.0, babysitter.getAvgRating(), 0.0001);
    }

    @Test
    void addReview_nonParent_throws() {
        User stranger = new User("Someone Else", "x@example.com", "h", "000", Role.PARENT);
        stranger.setId(99L);
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(completedBooking));

        assertThrows(ApiException.class,
                () -> reviewService.addReview(99L, 30L, request(5, "nope")));
    }

    @Test
    void addReview_notCompletedBooking_throws() {
        completedBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(completedBooking));

        assertThrows(ApiException.class,
                () -> reviewService.addReview(1L, 30L, request(5, "too early")));
    }

    @Test
    void addReview_bookingAlreadyReviewed_throws() {
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(completedBooking));
        when(reviewRepository.findByBookingId(30L)).thenReturn(Optional.of(new Review()));

        assertThrows(ApiException.class,
                () -> reviewService.addReview(1L, 30L, request(4, "again")));
    }

    @Test
    void listForBabysitter_filtersBySitter() {
        User otherUser = new User("Zed", "zed@example.com", "h", "0", Role.BABYSITTER);
        otherUser.setId(3L);
        Babysitter other = new Babysitter(otherUser, "Other", 1, new BigDecimal("10.00"));
        other.setId(6L);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Booking otherBooking = new Booking(parent, other, completedBooking.getSlot(),
                start, start.plusHours(2), new BigDecimal("20.00"), null);
        otherBooking.setId(31L);
        Review mine = new Review(completedBooking, parent, 5, "Great");
        mine.setId(9L);
        Review theirs = new Review(otherBooking, parent, 2, "Meh");
        theirs.setId(10L);
        when(reviewRepository.findAll()).thenReturn(List.of(mine, theirs));

        var result = reviewService.listForBabysitter(5L);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getRating());
    }

    private ReviewRequest request(int rating, String comment) {
        ReviewRequest req = new ReviewRequest();
        req.setRating(rating);
        req.setComment(comment);
        return req;
    }
}
