package com.pranit.notification.mail.service.impl;

import com.pranit.notification.mail.dto.EmailAttachment;
import com.pranit.notification.mail.dto.EmailRequest;
import com.pranit.notification.mail.dto.Recipient;
import com.pranit.notification.mail.service.MailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailSenderImpl implements MailSender {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${mail.from}")
    private String from;

    @Override
    public void send(EmailRequest request) {
        log.info("Sending email eventId={} template={}", request.eventId(), request.templateName());

        // Failures surface as Spring MailException (MailSendException, MailAuthenticationException, ...)
        javaMailSender.send(message -> populate(message, request));

        log.info("Email accepted by SMTP server eventId={}", request.eventId());
    }

    private void populate(MimeMessage message, EmailRequest request) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(
                message, MimeMessageHelper.MULTIPART_MODE_MIXED, StandardCharsets.UTF_8.name());

        Recipient recipient = request.recipient();

        helper.setFrom(from);
        helper.setTo(recipient.to());
        if (!recipient.cc().isEmpty()) {
            helper.setCc(recipient.cc().toArray(String[]::new));
        }
        if (!recipient.bcc().isEmpty()) {
            helper.setBcc(recipient.bcc().toArray(String[]::new));
        }
        helper.setSubject(request.subject());

        Context context = new Context(Locale.ENGLISH, request.templateData());
        helper.setText(templateEngine.process(request.templateName(), context), true);

        for (EmailAttachment attachment : request.attachments()) {
            helper.addAttachment(
                    attachment.filename(),
                    new ByteArrayResource(attachment.data()),
                    attachment.contentType());
        }

        // Trace a delivered email back to its audit row
        message.setHeader("X-Event-Id", request.eventId());
    }
}