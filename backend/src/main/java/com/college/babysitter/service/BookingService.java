package com.college.babysitter.service;

import com.college.babysitter.dto.BookingDto;
import com.college.babysitter.dto.BookingRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.*;
import com.college.babysitter.repository.AvailabilitySlotRepository;
import com.college.babysitter.repository.BabysitterRepository;
import com.college.babysitter.repository.BookingRepository;
import com.college.babysitter.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Core booking engine: slot claiming, pricing (rate x hours) and the
 * PENDING -&gt; CONFIRMED -&gt; COMPLETED/CANCELLED lifecycle with role checks.
 * Every state change notifies the affected party.
 */
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BabysitterRepository babysitterRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final NotificationService notificationService;

    /**
     * Wires the booking collaborators (constructor injection keeps the class unit-testable).
     *
     * @param bookingRepository booking persistence
     * @param userRepository parent lookup
     * @param babysitterRepository sitter lookup
     * @param slotRepository slot claiming
     * @param notificationService booking event notifications
     */
    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          BabysitterRepository babysitterRepository,
                          AvailabilitySlotRepository slotRepository,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.babysitterRepository = babysitterRepository;
        this.slotRepository = slotRepository;
        this.notificationService = notificationService;
    }

    /**
     * Creates a pending booking and locks the slot in the same transaction,
     * which is what guarantees a slot can never be double-booked.
     *
     * @param parentId id of the booking parent
     * @param request sitter id, slot id and optional notes
     * @return the pending booking with its computed total
     * @throws ApiException with 400 for unverified sitters, foreign/already-booked/past slots
     */
    @Transactional
    public BookingDto createBooking(Long parentId, BookingRequest request) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

        Babysitter babysitter = babysitterRepository.findById(request.getBabysitterId())
                .orElseThrow(() -> new ResourceNotFoundException("Babysitter not found"));
        if (!babysitter.isVerified()) {
            throw ApiException.badRequest("This babysitter is not yet verified");
        }

        AvailabilitySlot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        if (!slot.getBabysitter().getId().equals(babysitter.getId())) {
            throw ApiException.badRequest("Slot does not belong to this babysitter");
        }
        if (slot.isBooked()) {
            throw ApiException.badRequest("This slot has already been booked");
        }
        if (!slot.getStartTime().isAfter(LocalDateTime.now())) {
            throw ApiException.badRequest("This slot is in the past");
        }

        // the slot is claimed here so a concurrent request cannot grab it too
        slot.setBooked(true);
        slotRepository.save(slot);

        Booking booking = new Booking(
                parent,
                babysitter,
                slot,
                slot.getStartTime(),
                slot.getEndTime(),
                computeAmount(babysitter, slot),
                request.getNotes());
        bookingRepository.save(booking);

        notificationService.create(
                babysitter.getUser().getId(),
                "New booking request",
                parent.getFullName() + " wants to book you from "
                        + slot.getStartTime() + " to " + slot.getEndTime());
        notificationService.create(
                parent.getId(),
                "Booking created",
                "Your booking with " + babysitter.getUser().getFullName()
                        + " is pending confirmation");

        log.info("Booking created: parent {} booked babysitter {} (slot {})",
                parent.getId(), babysitter.getId(), slot.getId());
        return BookingDto.from(booking);
    }

    /**
     * Moves a booking through its lifecycle. Only the owning sitter can
     * confirm; completion needs an involved party; cancellation frees the slot.
     *
     * @param actorId id of the user performing the transition
     * @param actorRole role of that user
     * @param bookingId booking id
     * @param newStatus target status
     * @return the updated booking
     * @throws ApiException with 403 for wrong-party actions, 400 for illegal transitions
     */
    @Transactional
    public BookingDto updateStatus(Long actorId, Role actorRole, Long bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        switch (newStatus) {
            case CONFIRMED -> confirm(booking, actorId, actorRole);
            case COMPLETED -> complete(booking, actorId, actorRole);
            case CANCELLED -> cancel(booking, actorId, actorRole);
            default -> throw ApiException.badRequest("Unsupported status transition");
        }

        bookingRepository.save(booking);
        log.info("Booking {} moved to {} by {} {}", booking.getId(), newStatus, actorRole, actorId);
        return BookingDto.from(booking);
    }

    private void confirm(Booking booking, Long actorId, Role actorRole) {
        boolean isBabysitter = actorRole == Role.BABYSITTER
                && booking.getBabysitter().getUser().getId().equals(actorId);
        if (!isBabysitter) {
            throw ApiException.forbidden("Only the babysitter can confirm a booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw ApiException.badRequest("Only pending bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        notificationService.create(booking.getParent().getId(),
                "Booking confirmed",
                booking.getBabysitter().getUser().getFullName() + " confirmed your booking");
    }

    private void complete(Booking booking, Long actorId, Role actorRole) {
        boolean involved = (actorRole == Role.PARENT && booking.getParent().getId().equals(actorId))
                || (actorRole == Role.BABYSITTER && booking.getBabysitter().getUser().getId().equals(actorId));
        if (!involved) {
            throw ApiException.forbidden("You are not part of this booking");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw ApiException.badRequest("Only confirmed bookings can be marked complete");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        notificationService.create(booking.getParent().getId(),
                "Booking completed",
                "You can now review " + booking.getBabysitter().getUser().getFullName());
    }

    private void cancel(Booking booking, Long actorId, Role actorRole) {
        boolean involved = (actorRole == Role.PARENT && booking.getParent().getId().equals(actorId))
                || (actorRole == Role.BABYSITTER && booking.getBabysitter().getUser().getId().equals(actorId))
                || actorRole == Role.ADMIN;
        if (!involved) {
            throw ApiException.forbidden("You are not part of this booking");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw ApiException.badRequest("This booking can no longer be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.getSlot().setBooked(false); // slot goes back on the market
        slotRepository.save(booking.getSlot());
        notificationService.create(booking.getBabysitter().getUser().getId(),
                "Booking cancelled",
                "A booking on " + booking.getStartTime() + " was cancelled");
    }

    /**
     * Lists bookings for a user: own bookings for parents, incoming bookings
     * for sitters (empty when the sitter has no profile yet).
     *
     * @param userId the user id
     * @param role the user role, which selects the query
     * @return bookings newest first
     */
    @Transactional(readOnly = true)
    public List<BookingDto> listForUser(Long userId, Role role) {
        if (role == Role.BABYSITTER) {
            return babysitterRepository.findByUserId(userId)
                    .map(b -> bookingRepository.findByBabysitterIdOrderByCreatedAtDesc(b.getId()))
                    .orElse(List.of())
                    .stream().map(BookingDto::from).toList();
        }
        return bookingRepository.findByParentIdOrderByCreatedAtDesc(userId)
                .stream().map(BookingDto::from).toList();
    }

    private BigDecimal computeAmount(Babysitter babysitter, AvailabilitySlot slot) {
        double hours = Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes() / 60.0;
        return babysitter.getHourlyRate()
                .multiply(BigDecimal.valueOf(hours))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
