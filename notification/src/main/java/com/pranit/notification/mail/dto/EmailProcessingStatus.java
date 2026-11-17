package com.pranit.notification.mail.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pranit.notification.mail.entity.NotificationAudit;
import com.pranit.notification.mail.model.ProcessingStatus;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;

/**
 * Status snapshot of an email event, mirrors notification_audits.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailProcessingStatus(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("status") ProcessingStatus status,
        @JsonProperty("recipient") String recipient,
        @JsonProperty("errorMessage") String errorMessage,
        @JsonProperty("attempts") Integer attempts,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        @JsonProperty("startedAt") Instant startedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        @JsonProperty("finishedAt") Instant finishedAt
) implements Serializable {

    public static EmailProcessingStatus from(NotificationAudit audit) {
        return new EmailProcessingStatus(
                audit.getEventId(),
                audit.getStatus(),
                audit.getSendTo(),
                audit.getErrorMessage(),
                audit.getAttempts(),
                audit.getStart(),
                audit.getFinish()
        );
    }
}