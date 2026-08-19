package com.college.babysitter.service;

import com.college.babysitter.dto.BabysitterDto;
import com.college.babysitter.dto.BookingDto;
import com.college.babysitter.dto.UserDto;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.AvailabilitySlot;
import com.college.babysitter.model.Babysitter;
import com.college.babysitter.model.Booking;
import com.college.babysitter.model.BookingStatus;
import com.college.babysitter.model.Role;
import com.college.babysitter.model.User;
import com.college.babysitter.repository.AvailabilitySlotRepository;
import com.college.babysitter.repository.BabysitterRepository;
import com.college.babysitter.repository.BookingRepository;
import com.college.babysitter.repository.NotificationRepository;
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
class AdminServiceTest {

    @Mock
    private BabysitterRepository babysitterRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private AvailabilitySlotRepository slotRepository;

    @InjectMocks
    private AdminService adminService;

    private User parent;
    private User sitterUser;
    private Babysitter verified;
    private Babysitter pending;

    @BeforeEach
    void setUp() {
        parent = new User("Maria Lopez", "maria@example.com", "hashed", "555-0100", Role.PARENT);
        parent.setId(1L);
        sitterUser = new User("Ana Torres", "ana@example.com", "hashed", "555-0200", Role.BABYSITTER);
        sitterUser.setId(2L);
        verified = new Babysitter(sitterUser, "Experienced", 5, new BigDecimal("25.00"));
        verified.setId(5L);
        verified.setVerified(true);
        pending = new Babysitter(sitterUser, "New here", 1, new BigDecimal("12.00"));
        pending.setId(6L);
        pending.setVerified(false);
    }

    @Test
    void pendingBabysitters_returnsOnlyUnverified() {
        when(babysitterRepository.findAll()).thenReturn(List.of(verified, pending));

        List<BabysitterDto> result = adminService.pendingBabysitters();

        assertEquals(1, result.size());
        assertEquals(6L, result.get(0).getId());
    }

    @Test
    void verify_approvesBabysitter() {
        when(babysitterRepository.findById(6L)).thenReturn(Optional.of(pending));
        when(babysitterRepository.save(any(Babysitter.class))).thenAnswer(inv -> inv.getArgument(0));

        BabysitterDto dto = adminService.verify(6L, true);

        assertTrue(dto.isVerified());
    }

    @Test
    void verify_missing_throwsNotFound() {
        when(babysitterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.verify(99L, true));
    }

    @Test
    void allBookings_returnsNewestFirst() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        AvailabilitySlot slot = new AvailabilitySlot(verified, start, start.plusHours(2));
        slot.setId(7L);
        Booking older = new Booking(parent, verified, slot, start, start.plusHours(2),
                new BigDecimal("50.00"), null);
        older.setId(30L);
        older.setStatus(BookingStatus.CONFIRMED);
        older.setCreatedAt(LocalDateTime.now().minusHours(2));
        Booking newer = new Booking(parent, verified, slot, start, start.plusHours(2),
                new BigDecimal("50.00"), null);
        newer.setId(31L);
        newer.setStatus(BookingStatus.PENDING);
        newer.setCreatedAt(LocalDateTime.now());
        when(bookingRepository.findAll()).thenReturn(List.of(older, newer));

        List<BookingDto> result = adminService.allBookings();

        assertEquals(2, result.size());
        assertEquals(31L, result.get(0).getId());
        assertEquals(30L, result.get(1).getId());
    }

    @Test
    void userCount_delegatesToRepository() {
        when(userRepository.count()).thenReturn(7L);

        assertEquals(7L, adminService.userCount());
    }

    @Test
    void allBabysitters_returnsAll() {
        when(babysitterRepository.findAll()).thenReturn(List.of(verified, pending));

        assertEquals(2, adminService.allBabysitters().size());
    }

    @Test
    void allUsers_returnsNewestFirst() {
        parent.setCreatedAt(LocalDateTime.now().minusDays(1));
        sitterUser.setCreatedAt(LocalDateTime.now());
        when(userRepository.findAll()).thenReturn(List.of(parent, sitterUser));

        List<UserDto> result = adminService.allUsers();

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(Role.BABYSITTER, result.get(0).getRole());
    }

    @Test
    void deleteUser_parentWithoutBookings_removesAccount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(bookingRepository.findByParentIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(babysitterRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        adminService.deleteUser(9L, 1L);

        verify(userRepository).delete(parent);
    }

    @Test
    void deleteUser_sitterWithoutBookings_removesProfileAndSlots() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        AvailabilitySlot slot = new AvailabilitySlot(verified, start, start.plusHours(2));
        slot.setId(7L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(sitterUser));
        when(bookingRepository.findByParentIdOrderByCreatedAtDesc(2L)).thenReturn(List.of());
        when(babysitterRepository.findByUserId(2L)).thenReturn(Optional.of(verified));
        when(bookingRepository.findByBabysitterIdOrderByCreatedAtDesc(5L)).thenReturn(List.of());
        when(slotRepository.findByBabysitterIdOrderByStartTimeAsc(5L)).thenReturn(List.of(slot));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of());

        adminService.deleteUser(9L, 2L);

        verify(slotRepository).deleteAll(List.of(slot));
        verify(babysitterRepository).delete(verified);
        verify(userRepository).delete(sitterUser);
    }

    @Test
    void deleteUser_ownAccount_throws() {
        User admin = new User("Admin", "admin@babysitter.app", "h", "0", Role.ADMIN);
        admin.setId(9L);
        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));

        assertThrows(ApiException.class, () -> adminService.deleteUser(9L, 9L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_adminTarget_throwsForbidden() {
        User otherAdmin = new User("Admin", "admin@babysitter.app", "h", "0", Role.ADMIN);
        otherAdmin.setId(8L);
        when(userRepository.findById(8L)).thenReturn(Optional.of(otherAdmin));

        assertThrows(ApiException.class, () -> adminService.deleteUser(9L, 8L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_parentWithBookings_throws() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        AvailabilitySlot slot = new AvailabilitySlot(verified, start, start.plusHours(2));
        slot.setId(7L);
        Booking booking = new Booking(parent, verified, slot, start, start.plusHours(2),
                new BigDecimal("50.00"), null);
        booking.setId(30L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(bookingRepository.findByParentIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(booking));

        assertThrows(ApiException.class, () -> adminService.deleteUser(9L, 1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_sitterWithBookings_throws() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        AvailabilitySlot slot = new AvailabilitySlot(verified, start, start.plusHours(2));
        slot.setId(7L);
        Booking booking = new Booking(parent, verified, slot, start, start.plusHours(2),
                new BigDecimal("50.00"), null);
        booking.setId(30L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(sitterUser));
        when(bookingRepository.findByParentIdOrderByCreatedAtDesc(2L)).thenReturn(List.of());
        when(babysitterRepository.findByUserId(2L)).thenReturn(Optional.of(verified));
        when(bookingRepository.findByBabysitterIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(booking));

        assertThrows(ApiException.class, () -> adminService.deleteUser(9L, 2L));
        verify(userRepository, never()).delete(any());
        verify(babysitterRepository, never()).delete(any());
    }

    @Test
    void deleteUser_missing_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.deleteUser(9L, 99L));
    }
}
