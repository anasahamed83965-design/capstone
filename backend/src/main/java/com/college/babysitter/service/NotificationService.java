package com.college.babysitter.service;

import com.college.babysitter.dto.NotificationDto;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.Notification;
import com.college.babysitter.model.User;
import com.college.babysitter.repository.NotificationRepository;
import com.college.babysitter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * In-app notification inbox plus best-effort email fan-out. Booking events
 * call {@link #create} so both parties hear about every state change.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    /**
     * Wires the notification collaborators (constructor injection keeps the class unit-testable).
     *
     * @param notificationRepository notification persistence
     * @param userRepository recipient lookup
     * @param mailService best-effort email fan-out
     */
    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               MailService mailService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    /**
     * Stores an in-app notification and mirrors it to email. Email failures
     * never fail the caller — the inbox row is the source of truth.
     *
     * @param userId recipient account id
     * @param title short notification title
     * @param message notification body
     */
    @Transactional
    public void create(Long userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        notificationRepository.save(new Notification(user, title, message));
        mailService.send(user.getEmail(), title, message);
    }

    /**
     * Lists a user's notifications, newest first.
     *
     * @param userId account id
     * @return the user's notifications
     */
    public List<NotificationDto> listForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto::from)
                .toList();
    }

    /**
     * Counts a user's unread notifications (drives inbox badges).
     *
     * @param userId account id
     * @return number of unread notifications
     */
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Marks every notification of a user as read.
     *
     * @param userId account id
     */
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(n -> n.setRead(true));
    }
}
