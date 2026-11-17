package com.pranit.notification.mail.router.impl;

import com.pranit.notification.mail.dto.EmailRequest;
import com.pranit.notification.mail.event.EmailEvent;
import com.pranit.notification.mail.exception.InvalidEmailEventException;
import com.pranit.notification.mail.handler.EmailHandler;
import com.pranit.notification.mail.model.EmailPurpose;
import com.pranit.notification.mail.router.EmailRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Routes email events to their corresponding {@link EmailHandler} while enforcing lifecycle auditing.
 */
@Slf4j
@Component
public class EmailRouterImpl implements EmailRouter {

    private final Map<EmailPurpose, EmailHandler> handlers;

    public EmailRouterImpl(final List<EmailHandler> handlerList) {
        final Map<EmailPurpose, EmailHandler> map = new EnumMap<>(EmailPurpose.class);
        for (final EmailHandler handler : handlerList) {
            final EmailHandler previous = map.put(handler.supports(), handler);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate EmailHandler for purpose %s: %s and %s".formatted(
                                handler.supports(),
                                previous.getClass().getSimpleName(),
                                handler.getClass().getSimpleName()));
            }
        }
        this.handlers = Collections.unmodifiableMap(map);
        log.info("Registered email handlers: {}", this.handlers.keySet());
    }

    @Override
    @Async("emailExecutor")
    public void send(final EmailEvent event, final EmailPurpose purpose) {
        if (event == null || event.getEventId() == null || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("Email event and its eventId cannot be null or blank");
        }
        if (purpose == null) {
            throw new InvalidEmailEventException("Email purpose is required, eventId=" + event.getEventId());
        }
        if (!(event instanceof EmailRequest request)) {
            throw new InvalidEmailEventException
                    ("Unsupported event type %s, eventId: %s".formatted(
                            event.getClass().getSimpleName(), event.getEventId()));
        }
        if (request.purpose() != purpose) {
            throw new InvalidEmailEventException
                    ("Purpose mismatch: argument: %s, request: %s, eventId: %s".formatted(
                            purpose, request.purpose(), request.eventId()));
        }
        final EmailHandler handler = this.handlers.get(purpose);
        if (handler == null) {
            throw new InvalidEmailEventException
                    ("Unsupported email purpose: %s, eventId=%s".formatted(purpose, request.eventId()));
        }
        log.info("Routing email eventId: {} purpose: {} handler: {}",
                request.eventId(), purpose, handler.getClass().getSimpleName());
        try {
            handler.send(request);
        } catch (final RuntimeException ex) {
            log.error("Failed to process email eventId: {}: {}", request.eventId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}