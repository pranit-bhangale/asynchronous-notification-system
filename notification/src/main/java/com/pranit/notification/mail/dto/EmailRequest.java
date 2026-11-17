package com.pranit.notification.mail.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pranit.notification.mail.event.EmailEvent;
import com.pranit.notification.mail.model.EmailPurpose;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Payload for submitting a template-based email.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailRequest(

        @NotBlank(message = "eventId is required")
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

        @NotBlank(message = "templateName is required")
        @JsonProperty("templateName")
        String templateName,

        @JsonProperty("templateData")
        Map<String, Object> templateData,

        @JsonProperty("attachments")
        List<@Valid EmailAttachment> attachments
) implements EmailEvent, Serializable {

    public EmailRequest {
        templateData = templateData != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(templateData))
                : Map.of();
        attachments = attachments != null
                ? List.copyOf(attachments)
                : List.of();
    }

    public static EmailRequest ofTemplate(
            String eventId, EmailPurpose purpose, Recipient recipient,
            String subject, String templateName,
            Map<String, Object> templateData
    ) {
        return EmailRequest.builder()
                .eventId(eventId)
                .purpose(purpose)
                .recipient(recipient)
                .subject(subject)
                .templateName(templateName)
                .templateData(templateData)
                .build();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    @JsonIgnore
    public String getRecipientEmail() {
        return recipient != null ? recipient.to() : null;
    }

    @Override
    public EmailPurpose getPurpose() {
        return purpose;
    }
}