package com.college.babysitter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    void send_skipsWhenSenderNotConfigured() {
        ReflectionTestUtils.setField(mailService, "fromAddress", "");

        mailService.send("maria@example.com", "Hello", "Hi there");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_skipsBlankRecipient() {
        ReflectionTestUtils.setField(mailService, "fromAddress", "noreply@babysitter.app");

        mailService.send("  ", "Hello", "Hi there");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_sendsWhenConfigured() {
        ReflectionTestUtils.setField(mailService, "fromAddress", "noreply@babysitter.app");

        mailService.send("maria@example.com", "Hello", "Hi there");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_swallowsMailFailures() {
        ReflectionTestUtils.setField(mailService, "fromAddress", "noreply@babysitter.app");
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> mailService.send("maria@example.com", "Hello", "Hi there"));
    }
}
