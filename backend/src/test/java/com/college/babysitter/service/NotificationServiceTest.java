package com.college.babysitter.service;

import com.college.babysitter.dto.NotificationDto;
import com.college.babysitter.exception.ResourceNotFoundException;
import com.college.babysitter.model.Notification;
import com.college.babysitter.model.Role;
import com.college.babysitter.model.User;
import com.college.babysitter.repository.NotificationRepository;
import com.college.babysitter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MailService mailService;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Maria Lopez", "maria@example.com", "hashed", "555-0100", Role.PARENT);
        user.setId(1L);
    }

    @Test
    void create_savesAndSendsEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        notificationService.create(1L, "Booking confirmed", "Your booking is confirmed");

        verify(notificationRepository).save(any(Notification.class));
        verify(mailService).send(eq("maria@example.com"), eq("Booking confirmed"), eq("Your booking is confirmed"));
    }

    @Test
    void create_missingUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.create(99L, "Hi", "Hello"));
        verify(notificationRepository, never()).save(any());
        verify(mailService, never()).send(any(), any(), any());
    }

    @Test
    void listForUser_returnsDtos() {
        Notification first = new Notification(user, "One", "First message");
        first.setId(3L);
        Notification second = new Notification(user, "Two", "Second message");
        second.setId(4L);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(first, second));

        List<NotificationDto> result = notificationService.listForUser(1L);

        assertEquals(2, result.size());
        assertEquals("One", result.get(0).getTitle());
    }

    @Test
    void unreadCount_delegatesToRepository() {
        when(notificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(2L);

        assertEquals(2L, notificationService.unreadCount(1L));
    }

    @Test
    void markAllRead_marksEveryNotification() {
        Notification first = new Notification(user, "One", "First message");
        Notification second = new Notification(user, "Two", "Second message");
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(first, second));

        notificationService.markAllRead(1L);

        assertTrue(first.isRead());
        assertTrue(second.isRead());
    }
}
