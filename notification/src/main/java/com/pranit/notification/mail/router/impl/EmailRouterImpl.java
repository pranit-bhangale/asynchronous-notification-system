package com.pranit.notification.mail.router.impl;

import com.pranit.notification.mail.event.EmailEvent;
import com.pranit.notification.mail.exception.InvalidEmailEventException;
import com.pranit.notification.mail.handler.EmailHandler;
import com.pranit.notification.mail.model.EmailPurpose;
import com.pranit.notification.mail.router.EmailRouter;
import com.pranit.notification.mail.service.NotificationAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Routes email events to their corresponding {@link EmailHandler} while enforcing lifecycle auditing.
 */
@Slf4j
@Component
public class EmailRouterImpl implements EmailRouter {

    private final Map<EmailPurpose, EmailHandler<?>> handlers;
    private final NotificationAuditService auditService;

    public EmailRouterImpl(List<EmailHandler<?>> handlers, NotificationAuditService auditService) {
        this.handlers = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(EmailHandler::supports, Function.identity()));
        this.auditService = auditService;
    }

    @Async("emailExecutor")
    @Override
    public void send(final EmailEvent event, final EmailPurpose purpose) {
        if (event == null || event.getEventId() == null || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("Email event and its client-supplied eventId cannot be null or blank");
        }
        final String threadName = Thread.currentThread().getName();
        this.auditService.recordStart(event, threadName);
        final EmailHandler<?> handler = this.handlers.get(purpose);
        if (handler == null) {
            final String errorMsg = "Unsupported email purpose: " + purpose;
            this.auditService.recordFinishFailure(event.getEventId(), "UNSUPPORTED_PURPOSE", errorMsg);
            throw new InvalidEmailEventException(errorMsg);
        }
        try {
            sendEvent(handler, event);
            this.auditService.recordFinishSuccess(event.getEventId(),
                    String.format("Email dispatched successfully by handler [%s]", handler.getClass().getSimpleName()));
        } catch (Exception ex) {
            this.auditService.recordFinishFailure(event.getEventId(),
                    ex.getClass().getSimpleName(), ex.getMessage(), "Email processing failed: " + ex.getMessage());
            log.error("Failed to process email eventId [{}]: {}", event.getEventId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void sendEvent(final EmailHandler<T> handler, final EmailEvent event) {
        handler.send((T) event);
    }
}
