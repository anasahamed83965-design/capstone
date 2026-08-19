package com.college.babysitter.dto;

import com.college.babysitter.model.AvailabilitySlot;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Availability slot view: times plus whether it is already booked.
 */
@Data
@AllArgsConstructor
public class SlotDto {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean booked;

    /**
     * Converts a slot entity to the view.
     *
     * @param slot slot entity
     * @return the slot view
     */
    public static SlotDto from(AvailabilitySlot slot) {
        return new SlotDto(slot.getId(), slot.getStartTime(), slot.getEndTime(), slot.isBooked());
    }
}
