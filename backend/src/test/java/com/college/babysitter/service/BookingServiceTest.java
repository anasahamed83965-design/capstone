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
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BabysitterRepository babysitterRepository;
    @Mock
    private AvailabilitySlotRepository slotRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    private User parent;
    private User sitterUser;
    private Babysitter babysitter;
    private AvailabilitySlot slot;

    @BeforeEach
    void setUp() {
        parent = new User("Maria Lopez", "maria@example.com", "hashed", "555-0100", Role.PARENT);
        parent.setId(1L);
        sitterUser = new User("Ana Torres", "ana@example.com", "hashed", "555-0200", Role.BABYSITTER);
        sitterUser.setId(2L);
        babysitter = new Babysitter(sitterUser, "Friendly and reliable", 3, new BigDecimal("20.00"));
        babysitter.setId(5L);
        babysitter.setVerified(true);

        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        slot = new AvailabilitySlot(babysitter, start, start.plusHours(2));
        slot.setId(7L);
    }

    @Test
    void createBooking_computesAmountAndMarksSlot() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(babysitterRepository.findById(5L)).thenReturn(Optional.of(babysitter));
        when(slotRepository.findById(7L)).thenReturn(Optional.of(slot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(30L);
            return b;
        });

        BookingDto dto = bookingService.createBooking(1L, request());

        assertTrue(slot.isBooked());
        assertEquals(new BigDecimal("40.00"), dto.getTotalAmount());
        assertEquals(BookingStatus.PENDING, dto.getStatus());
        verify(slotRepository).save(slot);
        verify(notificationService, times(2)).create(anyLong(), anyString(), anyString());
    }

    @Test
    void createBooking_unverifiedBabysitter_throws() {
        babysitter.setVerified(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(babysitterRepository.findById(5L)).thenReturn(Optional.of(babysitter));

        ApiException ex = assertThrows(ApiException.class,
                () -> bookingService.createBooking(1L, request()));

        assertTrue(ex.getMessage().toLowerCase().contains("not yet verified"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_alreadyBookedSlot_throws() {
        slot.setBooked(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(babysitterRepository.findById(5L)).thenReturn(Optional.of(babysitter));
        when(slotRepository.findById(7L)).thenReturn(Optional.of(slot));

        assertThrows(ApiException.class, () -> bookingService.createBooking(1L, request()));
    }

    @Test
    void createBooking_slotNotOwnedByBabysitter_throws() {
        Babysitter other = new Babysitter(sitterUser, "Other", 1, new BigDecimal("15.00"));
        other.setId(6L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(babysitterRepository.findById(5L)).thenReturn(Optional.of(babysitter));
        when(slotRepository.findById(7L)).thenReturn(Optional.of(slot));
        slot.setBabysitter(other);

        assertThrows(ApiException.class, () -> bookingService.createBooking(1L, request()));
    }

    @Test
    void updateStatus_confirmOnlyByOwningBabysitter() {
        Booking booking = booking(BookingStatus.PENDING);
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));

        bookingService.updateStatus(2L, Role.BABYSITTER, 30L, BookingStatus.CONFIRMED);

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateStatus_parentCannotConfirm_throws() {
        Booking booking = booking(BookingStatus.PENDING);
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));

        assertThrows(ApiException.class,
                () -> bookingService.updateStatus(1L, Role.PARENT, 30L, BookingStatus.CONFIRMED));
    }

    @Test
    void updateStatus_completeOnlyFromConfirmed() {
        Booking booking = booking(BookingStatus.PENDING);
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));

        assertThrows(ApiException.class,
                () -> bookingService.updateStatus(1L, Role.PARENT, 30L, BookingStatus.COMPLETED));
    }

    @Test
    void updateStatus_cancelFreesSlot() {
        slot.setBooked(true);
        Booking booking = booking(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(30L)).thenReturn(Optional.of(booking));

        bookingService.updateStatus(1L, Role.PARENT, 30L, BookingStatus.CANCELLED);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertFalse(slot.isBooked());
        verify(slotRepository).save(slot);
    }

    @Test
    void listForUser_parentReturnsOwnBookings() {
        Booking booking = booking(BookingStatus.CONFIRMED);
        when(bookingRepository.findByParentIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(booking));

        var result = bookingService.listForUser(1L, Role.PARENT);

        assertEquals(1, result.size());
        assertEquals(30L, result.get(0).getId());
        assertEquals("Ana Torres", result.get(0).getBabysitterName());
    }

    @Test
    void listForUser_babysitterReturnsOwnBookings() {
        Booking booking = booking(BookingStatus.PENDING);
        when(babysitterRepository.findByUserId(2L)).thenReturn(Optional.of(babysitter));
        when(bookingRepository.findByBabysitterIdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(booking));

        var result = bookingService.listForUser(2L, Role.BABYSITTER);

        assertEquals(1, result.size());
        assertEquals("Maria Lopez", result.get(0).getParentName());
    }

    @Test
    void listForUser_babysitterWithoutProfileReturnsEmpty() {
        when(babysitterRepository.findByUserId(9L)).thenReturn(Optional.empty());

        assertTrue(bookingService.listForUser(9L, Role.BABYSITTER).isEmpty());
        verify(bookingRepository, never()).findByBabysitterIdOrderByCreatedAtDesc(any());
    }

    private BookingRequest request() {
        BookingRequest req = new BookingRequest();
        req.setBabysitterId(5L);
        req.setSlotId(7L);
        return req;
    }

    private Booking booking(BookingStatus status) {
        Booking b = new Booking(parent, babysitter, slot,
                slot.getStartTime(), slot.getEndTime(), new BigDecimal("40.00"), "Bring snacks");
        b.setId(30L);
        b.setStatus(status);
        return b;
    }
}
