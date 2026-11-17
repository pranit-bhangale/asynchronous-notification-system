package com.pranit.notification.mail.subscriber;

import com.pranit.notification.mail.dto.EmailRequest;
import com.pranit.notification.mail.router.EmailRouter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSubscriberService {

    private final EmailRouter router;
    private final Validator validator;

    @Bean
    Consumer<EmailRequest> processEmail() {
        return request -> {
            if (request == null) {
                log.warn("Received null email message, skipping");
                return;
            }
            final Set<ConstraintViolation<EmailRequest>> violations = this.validator.validate(request);
            if (!violations.isEmpty()) {
                log.error("Invalid email message eventId: {} violations: {}",
                        request.eventId(),
                        violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .sorted()
                                .toList());
                return;
            }
            log.info("Received email message eventId: {} purpose: {}", request.eventId(), request.purpose());
            this.router.send(request, request.purpose());
        };
    }
}
