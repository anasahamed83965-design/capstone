package com.college.babysitter.service;

import com.college.babysitter.dto.SlotDto;
import com.college.babysitter.dto.SlotRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.AvailabilitySlot;
import com.college.babysitter.model.Babysitter;
import com.college.babysitter.model.Role;
import com.college.babysitter.model.User;
import com.college.babysitter.repository.AvailabilitySlotRepository;
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
class AvailabilityServiceTest {

    @Mock
    private AvailabilitySlotRepository slotRepository;
    @Mock
    private BabysitterService babysitterService;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Babysitter babysitter;

    @BeforeEach
    void setUp() {
        User sitterUser = new User("Ana Torres", "ana@example.com", "hashed", "555-0200", Role.BABYSITTER);
        sitterUser.setId(2L);
        babysitter = new Babysitter(sitterUser, "Friendly", 3, new BigDecimal("20.00"));
        babysitter.setId(5L);
        babysitter.setVerified(true);
    }

    @Test
    void addSlot_createsFutureSlot() {
        when(babysitterService.getMyProfile(2L)).thenReturn(babysitter);
        when(slotRepository.findByBabysitterIdOrderByStartTimeAsc(5L)).thenReturn(List.of());
        when(slotRepository.save(any(AvailabilitySlot.class))).thenAnswer(inv -> {
            AvailabilitySlot s = inv.getArgument(0);
            s.setId(7L);
            return s;
        });

        SlotDto dto = availabilityService.addSlot(2L, request(
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(2)));

        assertEquals(7L, dto.getId());
        assertFalse(dto.isBooked());
        verify(slotRepository).save(any(AvailabilitySlot.class));
    }

    @Test
    void addSlot_endBeforeStart_throws() {
        when(babysitterService.getMyProfile(2L)).thenReturn(babysitter);
        LocalDateTime start = LocalDateTime.now().plusDays(2);

        assertThrows(ApiException.class,
                () -> availabilityService.addSlot(2L, request(start, start.minusHours(1))));
        verify(slotRepository, never()).save(any());
    }

    @Test
    void addSlot_inThePast_throws() {
        when(babysitterService.getMyProfile(2L)).thenReturn(babysitter);

        assertThrows(ApiException.class, () -> availabilityService.addSlot(2L,
                request(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(2))));
        verify(slotRepository, never()).save(any());
    }

    @Test
    void addSlot_overlapping_throws() {
        LocalDateTime existingStart = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        AvailabilitySlot existing = new AvailabilitySlot(babysitter, existingStart, existingStart.plusHours(2));
        existing.setId(7L);
        when(babysitterService.getMyProfile(2L)).thenReturn(babysitter);
        when(slotRepository.findByBabysitterIdOrderByStartTimeAsc(5L)).thenReturn(List.of(existing));

        assertThrows(ApiException.class, () -> availabilityService.addSlot(2L,
                request(existingStart.plusHours(1), existingStart.plusHours(3))));
        verify(slotRepository, never()).save(any());
    }

    @Test
    void listMySlots_returnsOrdered() {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        AvailabilitySlot slot = new AvailabilitySlot(babysitter, start, start.plusHours(1));
        slot.setId(8L);
        when(babysitterService.getMyProfile(2L)).thenReturn(babysitter);
        when(slotRepository.findByBabysitterIdOrderByStartTimeAsc(5L)).thenReturn(List.of(slot));

        List<SlotDto> result = availabilityService.listMySlots(2L);

        assertEquals(1, result.size());
        assertEquals(8L, result.get(0).getId());
    }

    @Test
    void deleteSlot_removesOpenSlot() {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        AvailabilitySlot slot = new AvailabilitySlot(babysitter, start, start.plusHours(1));
        slot.setId(8L);
        when(babysitterService.getMyProfile(2L)).thenReturn(babysitter);
        when(slotRepository.findById(8L)).thenReturn(Optional.of(slot));

        availabilityService.deleteSlot(2L, 8L);

        verify(slotRepository).delete(slot);
    }

    @Test
    void deleteSlot_booked_throws() {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        AvailabilitySlot slot = new AvailabilitySlot(babysitter, start, start.plusHours(1));
        slot.setId(8L);
        slot.setBooked(true);
        when(babysitterService.getMyProfile(2L)).thenReturn(babysitter);
        when(slotRepository.findById(8L)).thenReturn(Optional.of(slot));

        assertThrows(ApiException.class, () -> availabilityService.deleteSlot(2L, 8L));
        verify(slotRepository, never()).delete(any());
    }

    @Test
    void deleteSlot_foreignSlot_throwsForbidden() {
        User otherUser = new User("Other", "o@example.com", "h", "0", Role.BABYSITTER);
        otherUser.setId(3L);
        Babysitter other = new Babysitter(otherUser, "Other", 1, new BigDecimal("10.00"));
        other.setId(6L);
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        AvailabilitySlot slot = new AvailabilitySlot(other, start, start.plusHours(1));
        slot.setId(8L);
        when(babysitterService.getMyProfile(2L)).thenReturn(babysitter);
        when(slotRepository.findById(8L)).thenReturn(Optional.of(slot));

        assertThrows(ApiException.class, () -> availabilityService.deleteSlot(2L, 8L));
    }

    @Test
    void deleteSlot_missing_throwsNotFound() {
        when(babysitterService.getMyProfile(2L)).thenReturn(babysitter);
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> availabilityService.deleteSlot(2L, 99L));
    }

    private SlotRequest request(LocalDateTime start, LocalDateTime end) {
        SlotRequest req = new SlotRequest();
        req.setStartTime(start);
        req.setEndTime(end);
        return req;
    }
}
