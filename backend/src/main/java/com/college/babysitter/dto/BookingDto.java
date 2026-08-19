package com.college.babysitter.dto;

import com.college.babysitter.model.Booking;
import com.college.babysitter.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Booking view with human names (parent/sitter) instead of bare ids, plus
 * the snapshotted times and computed total.
 */
@Data
@AllArgsConstructor
public class BookingDto {

    private Long id;
    private Long parentId;
    private String parentName;
    private Long babysitterId;
    private String babysitterName;
    private Long slotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;

    /**
     * Converts a booking entity (with parent, sitter and slot loaded) to the view.
     *
     * @param b booking entity
     * @return the booking view
     */
    public static BookingDto from(Booking b) {
        return new BookingDto(
                b.getId(),
                b.getParent().getId(),
                b.getParent().getFullName(),
                b.getBabysitter().getId(),
                b.getBabysitter().getUser().getFullName(),
                b.getSlot().getId(),
                b.getStartTime(),
                b.getEndTime(),
                b.getStatus(),
                b.getTotalAmount(),
                b.getNotes(),
                b.getCreatedAt());
    }
}
