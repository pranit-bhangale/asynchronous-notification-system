package com.pranit.notification.mail.handler.impl;

import com.pranit.notification.mail.dto.EmailRequest;
import com.pranit.notification.mail.exception.EmailValidationException;
import com.pranit.notification.mail.handler.EmailHandler;
import com.pranit.notification.mail.model.EmailPurpose;
import com.pranit.notification.mail.service.MailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Handles processing and SMTP dispatch for {@link EmailPurpose#PASSWORD_RESET} emails.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetEmailHandler implements EmailHandler {

    private static final String TEMPLATE = "mail/otp";
    private static final Set<String> REQUIRED_KEYS = Set.of("organizationName", "otpCode");

    private final MailSender mailSender;

    private static boolean isBlank(final Object value) {
        return value == null || value.toString().isBlank();
    }

    @Override
    public EmailPurpose supports() {
        return EmailPurpose.PASSWORD_RESET;
    }

    @Override
    public void send(final EmailRequest request) {
        final List<String> missing = REQUIRED_KEYS.stream()
                .filter(key -> isBlank(request.templateData().get(key)))
                .sorted()
                .toList();

        if (!missing.isEmpty()) {
            throw new EmailValidationException
                    ("OTP email missing template data: " + missing, request.eventId());
        }
        log.debug("Sending OTP email eventId: {}", request.eventId());
        this.mailSender.send(request, TEMPLATE);
    }
}