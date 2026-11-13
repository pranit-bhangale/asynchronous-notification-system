package com.pranit.notification.mail.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pranit.notification.mail.event.EmailEvent;
import com.pranit.notification.mail.model.EmailPriority;
import com.pranit.notification.mail.model.EmailPurpose;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Concrete record specialized for template-based emails using dynamic key-value variables.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TemplateEmailRequest(
        @NotBlank(message = "eventId must be provided by the client and cannot be blank")
        @JsonProperty("eventId")
        String eventId,

        @NotNull(message = "Email purpose is required")
        @JsonProperty("purpose")
        EmailPurpose purpose,

        @NotNull(message = "Recipient information is required")
        @Valid
        @JsonProperty("recipient")
        Recipient recipient,

        @NotBlank(message = "Email subject is required")
        @JsonProperty("subject")
        String subject,

        @JsonProperty("templateName")
        String templateName,

        @JsonProperty("templateData")
        Map<String, Object> templateData,

        @JsonProperty("priority")
        EmailPriority priority,

        @JsonProperty("from")
        String from,

        @JsonProperty("replyTo")
        String replyTo,

        @JsonProperty("attachments")
        List<EmailAttachment> attachments,

        @JsonProperty("metadata")
        Map<String, String> metadata,

        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        @JsonProperty("requestedAt")
        Instant requestedAt
) implements EmailEvent, Serializable {

    public TemplateEmailRequest {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must be provided by the client and cannot be null or blank");
        }
        if (priority == null) {
            priority = EmailPriority.NORMAL;
        }
        templateData = templateData != null ? Map.copyOf(templateData) : Map.of();
        attachments = attachments != null ? List.copyOf(attachments) : List.of();
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getRecipientEmail() {
        return recipient != null ? recipient.to() : null;
    }

    @Override
    public EmailPurpose getPurpose() {
        return purpose;
    }

    public EmailRequest<Map<String, Object>> toGenericRequest() {
        return new EmailRequest<>(
                eventId, purpose, recipient, subject, templateName,
                templateData, priority, from, replyTo, attachments, metadata, requestedAt
        );
    }
}
