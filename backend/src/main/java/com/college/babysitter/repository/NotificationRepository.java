package com.college.babysitter.repository;

import com.college.babysitter.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Notification inbox persistence.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Lists a user's notifications, newest first.
     *
     * @param userId account id
     * @return the user's notifications
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Counts a user's unread notifications (inbox badge).
     *
     * @param userId account id
     * @return unread count
     */
    long countByUserIdAndReadFalse(Long userId);
}
