package com.pranit.notification.mail.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pranit.notification.mail.model.EmailPurpose;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * Status snapshot of an email event's lifecycle.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailProcessingStatus(
        @JsonProperty("eventId")
        String eventId,

        @JsonProperty("status")
        com.pranit.notification.mail.model.EmailProcessingStatus status,

        @JsonProperty("stage")
        String stage,

        @JsonProperty("recipient")
        String recipient,

        @JsonProperty("purpose")
        EmailPurpose purpose,

        @JsonProperty("templateName")
        String templateName,

        @JsonProperty("attemptNumber")
        Integer attemptNumber,

        @JsonProperty("statusDetails")
        String statusDetails,

        @JsonProperty("threadName")
        String threadName,

        @JsonProperty("hostName")
        String hostName,

        @JsonProperty("executionTimeMs")
        Long executionTimeMs,

        @JsonProperty("errorCode")
        String errorCode,

        @JsonProperty("errorMessage")
        String errorMessage,

        @JsonProperty("metadata")
        Map<String, String> metadata,

        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        @JsonProperty("createdAt")
        Instant createdAt
) implements Serializable {

    public EmailProcessingStatus {
        attemptNumber = attemptNumber != null ? attemptNumber : 1;
        executionTimeMs = executionTimeMs != null ? executionTimeMs : 0L;
    }

    public String getEventId() {
        return eventId;
    }

    public com.pranit.notification.mail.model.EmailProcessingStatus getStatus() {
        return status;
    }

    public String getStage() {
        return stage;
    }

    public String getRecipient() {
        return recipient;
    }

    public EmailPurpose getPurpose() {
        return purpose;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public String getStatusDetails() {
        return statusDetails;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getHostName() {
        return hostName;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
