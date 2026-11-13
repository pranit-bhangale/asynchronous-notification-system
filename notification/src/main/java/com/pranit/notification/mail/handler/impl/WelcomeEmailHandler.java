package com.pranit.notification.mail.handler.impl;

import com.pranit.notification.mail.dto.EmailRequest;
import com.pranit.notification.mail.handler.EmailHandler;
import com.pranit.notification.mail.model.EmailPurpose;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Handles processing and SMTP dispatch for {@link EmailPurpose#WELCOME} emails.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeEmailHandler implements EmailHandler<EmailRequest<Map<String, Object>>> {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public EmailPurpose supports() {
        return EmailPurpose.WELCOME;
    }

    @Override
    public void send(EmailRequest<Map<String, Object>> event) {
        log.info("Processing welcome email eventId [{}] for recipient [{}]",
                event.eventId(), event.getRecipientEmail());

        // Populate Thymeleaf template variables from the generic map
        Context context = new Context();
        if (event.templateData() != null) {
            event.templateData().forEach(context::setVariable);
        }

        String templateName = event.templateName() != null ? event.templateName() : "welcome";
        String htmlContent = templateEngine.process(templateName, context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (event.from() != null && !event.from().isBlank()) {
                helper.setFrom(event.from());
            }
            helper.setTo(event.recipient().to());
            helper.setSubject(event.subject());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent successfully for eventId [{}]", event.eventId());
        } catch (Exception e) {
            log.error("Failed to send welcome email for eventId [{}]: {}", event.eventId(), e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
