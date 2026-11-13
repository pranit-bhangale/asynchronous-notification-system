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
 *
 * <p><b>Why it is implemented:</b></p>
 * <ul>
 *   <li><b>Strategy Pattern & Extensibility:</b> Dynamically collects all {@link EmailHandler} beans
 *       registered in the Spring context and indexes them by {@link EmailPurpose}. New email types
 *       can be added by creating a new handler class without modifying existing router logic.</li>
 *   <li><b>Virtual Thread Asynchrony:</b> Annotated with {@code @Async("emailExecutor")} so dispatching
 *       runs concurrently on dedicated virtual threads, freeing caller threads immediately.</li>
 *   <li><b>Guaranteed Audit Lifecycle:</b> Enforces that the {@code START} milestone is recorded
 *       immediately upon entering the background thread, and records either finish success ({@code SENT})
 *       or finish failure ({@code FAILED}) with execution duration and timestamps.</li>
 *   <li><b>Client-Supplied eventId Requirement:</b> Requires that every incoming event has a non-blank
 *       client-supplied {@code eventId} to guarantee end-to-end tracing.</li>
 * </ul>
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

        // 1. Audit START status immediately upon entering the virtual thread
        auditService.recordStart(event, threadName);

        final EmailHandler<?> handler = this.handlers.get(purpose);
        if (handler == null) {
            final String errorMsg = "Unsupported email purpose: " + purpose;
            auditService.recordFinishFailure(
                    event.getEventId(),
                    "UNSUPPORTED_PURPOSE",
                    errorMsg,
                    errorMsg
            );
            throw new InvalidEmailEventException(errorMsg);
        }

        try {
            sendEvent(handler, event);
            // 2. Audit successful finish
            auditService.recordFinishSuccess(
                    event.getEventId(),
                    String.format("Email dispatched successfully by handler [%s]", handler.getClass().getSimpleName())
            );
        } catch (Exception ex) {
            // 3. Audit failure finish with diagnostic details
            auditService.recordFinishFailure(
                    event.getEventId(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    "Email processing failed: " + ex.getMessage()
            );
            log.error("Failed to process email eventId [{}]: {}", event.getEventId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void sendEvent(EmailHandler<T> handler, EmailEvent event) {
        handler.send((T) event);
    }
}
