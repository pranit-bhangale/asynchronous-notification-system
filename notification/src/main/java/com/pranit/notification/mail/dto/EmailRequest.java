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
 * Primary payload envelope for submitting an email notification.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailRequest<T>(
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

        @Valid
        @JsonProperty("templateData")
        T templateData,

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

    /**
     * Compact constructor:
     * <ul>
     *   <li>Enforces that {@code eventId} is strictly supplied by the caller (no fallback/auto-generation).
     *       Throws {@link IllegalArgumentException} if null or blank.</li>
     *   <li>Ensures collections and priorities fall back to safe non-null defaults to prevent
     *       NullPointerExceptions in template renderers and handlers.</li>
     * </ul>
     */
    public EmailRequest {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must be provided by the client and cannot be null or blank");
        }
        if (priority == null) {
            priority = EmailPriority.NORMAL;
        }
        attachments = attachments != null ? List.copyOf(attachments) : List.of();
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }

    /**
     * Convenience factory for template-driven emails with dynamic key-value placeholders.
     */
    public static EmailRequest<Map<String, Object>> ofTemplate(
            String eventId,
            EmailPurpose purpose,
            Recipient recipient,
            String subject,
            String templateName,
            Map<String, Object> templateData) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must be provided by the client and cannot be null or blank");
        }
        return EmailRequest.<Map<String, Object>>builder()
                .eventId(eventId)
                .purpose(purpose)
                .recipient(recipient)
                .subject(subject)
                .templateName(templateName)
                .templateData(templateData != null ? templateData : Map.of())
                .priority(EmailPriority.NORMAL)
                .requestedAt(Instant.now())
                .build();
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

    // JavaBean getter aliases for template engine compatibility (Thymeleaf/SpEL)
    public String getSubject() {
        return subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public T getTemplateData() {
        return templateData;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public String getFrom() {
        return from;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public EmailPriority getPriority() {
        return priority;
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }
}
