package com.college.babysitter.controller;

import com.college.babysitter.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.college.babysitter.dto.NotificationDto;
import com.college.babysitter.security.AppUserPrincipal;
import com.college.babysitter.security.SecurityUtils;
import com.college.babysitter.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * In-app notification inbox for the logged-in user. Notifications are created
 * as a side effect of booking events (see {@code BookingService}).
 */
@Tag(name = "Notifications", description = "In-app inbox fed by booking events")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Lists the logged-in user's notifications, newest first.
     *
     * @param authentication the user's JWT authentication
     * @return the user's notifications
     */
    @GetMapping
    public ApiResponse<List<NotificationDto>> list(Authentication authentication) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(notificationService.listForUser(user.getId()));
    }

    /**
     * Counts the logged-in user's unread notifications (for badges).
     *
     * @param authentication the user's JWT authentication
     * @return number of unread notifications
     */
    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(Authentication authentication) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        return ApiResponse.ok(notificationService.unreadCount(user.getId()));
    }

    /**
     * Marks all of the logged-in user's notifications as read.
     *
     * @param authentication the user's JWT authentication
     * @return empty success envelope
     */
    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(Authentication authentication) {
        AppUserPrincipal user = SecurityUtils.currentUser(authentication);
        notificationService.markAllRead(user.getId());
        return ApiResponse.ok(null, "All notifications marked as read");
    }
}
