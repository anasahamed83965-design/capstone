package com.college.babysitter.service;

import com.college.babysitter.dto.SlotDto;
import com.college.babysitter.dto.SlotRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.AvailabilitySlot;
import com.college.babysitter.model.Babysitter;
import com.college.babysitter.repository.AvailabilitySlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sitter calendar logic: slot creation with overlap/past protection,
 * slot listings and safe deletion (booked slots are untouchable).
 */
@Service
public class AvailabilityService {

    private final AvailabilitySlotRepository slotRepository;
    private final BabysitterService babysitterService;

    /**
     * Wires the calendar collaborators (constructor injection keeps the class unit-testable).
     *
     * @param slotRepository slot persistence
     * @param babysitterService resolves the current sitter
     */
    public AvailabilityService(AvailabilitySlotRepository slotRepository, BabysitterService babysitterService) {
        this.slotRepository = slotRepository;
        this.babysitterService = babysitterService;
    }

    /**
     * Adds a slot after rejecting bad ranges, past times and overlaps, so a
     * sitter can never be double-booked through the calendar.
     *
     * @param userId sitter account id
     * @param request validated start and end time
     * @return the created slot
     * @throws ApiException with 400 for invalid or overlapping slots
     */
    @Transactional
    public SlotDto addSlot(Long userId, SlotRequest request) {
        Babysitter babysitter = babysitterService.getMyProfile(userId);

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw ApiException.badRequest("Start time must be before end time");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw ApiException.badRequest("Cannot create a slot in the past");
        }
        if (overlaps(babysitter, request)) {
            throw ApiException.badRequest("This slot overlaps an existing slot");
        }

        AvailabilitySlot slot = slotRepository.save(
                new AvailabilitySlot(babysitter, request.getStartTime(), request.getEndTime()));
        return SlotDto.from(slot);
    }

    /**
     * Lists all of a sitter's own slots, booked and open.
     *
     * @param userId sitter account id
     * @return the sitter's slots ordered by start time
     */
    public List<SlotDto> listMySlots(Long userId) {
        Babysitter babysitter = babysitterService.getMyProfile(userId);
        return slotRepository.findByBabysitterIdOrderByStartTimeAsc(babysitter.getId())
                .stream()
                .map(SlotDto::from)
                .toList();
    }

    /**
     * Lists a sitter's future unbooked slots for the parent booking form.
     *
     * @param babysitterId sitter id
     * @return open slots ordered by start time
     */
    public List<SlotDto> listOpenSlots(Long babysitterId) {
        return slotRepository.findByBabysitterIdAndBookedFalseAndStartTimeAfterOrderByStartTimeAsc(
                        babysitterId, LocalDateTime.now())
                .stream()
                .map(SlotDto::from)
                .toList();
    }

    /**
     * Deletes a sitter's own slot. Foreign slots are forbidden and booked
     * slots are protected because a booking already depends on them.
     *
     * @param userId sitter account id
     * @param slotId slot id
     * @throws ApiException with 403 for foreign slots, 400 for booked slots
     */
    @Transactional
    public void deleteSlot(Long userId, Long slotId) {
        Babysitter babysitter = babysitterService.getMyProfile(userId);
        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        if (!slot.getBabysitter().getId().equals(babysitter.getId())) {
            throw ApiException.forbidden("This slot does not belong to you");
        }
        if (slot.isBooked()) {
            throw ApiException.badRequest("Cannot delete a booked slot");
        }
        slotRepository.delete(slot);
    }

    private boolean overlaps(Babysitter babysitter, SlotRequest request) {
        return slotRepository.findByBabysitterIdOrderByStartTimeAsc(babysitter.getId()).stream()
                .filter(s -> !s.isBooked())
                .anyMatch(s -> s.getStartTime().isBefore(request.getEndTime())
                        && request.getStartTime().isBefore(s.getEndTime()));
    }
}
