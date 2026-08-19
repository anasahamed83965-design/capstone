package com.college.babysitter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Best-effort SMTP email sender backing booking notifications. When SMTP is
 * unconfigured or down, failures are only logged so bookings never break.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    /**
     * Wires the mail sender (constructor injection keeps the class unit-testable).
     *
     * @param mailSender Spring mail sender
     */
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Best-effort email. SMTP may not be configured in dev, so failures are
     * logged and never bubble up - the booking still succeeds.
     */
    public void send(String to, String subject, String body) {
        if (to == null || to.isBlank() || fromAddress.isBlank()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Email not sent to {}: {}", to, ex.getMessage());
        }
    }
}
