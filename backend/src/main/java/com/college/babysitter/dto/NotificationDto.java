package com.college.babysitter.dto;

import com.college.babysitter.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Inbox row view: title, message and read flag.
 */
@Data
@AllArgsConstructor
public class NotificationDto {

    private Long id;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    /**
     * Converts a notification entity to the inbox view.
     *
     * @param notification notification entity
     * @return the inbox view
     */
    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
