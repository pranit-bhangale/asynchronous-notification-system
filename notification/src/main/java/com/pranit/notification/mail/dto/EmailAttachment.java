package com.pranit.notification.mail.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.io.Serializable;

/**
 * File attached to an email. {@code data} is Base64 in JSON.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailAttachment(

        @NotBlank(message = "Attachment filename is required")
        @JsonProperty("filename")
        String filename,

        @NotBlank(message = "Attachment content type (MIME) is required")
        @JsonProperty("contentType")
        String contentType,

        @NotEmpty(message = "Attachment data is required")
        @JsonProperty("data")
        byte[] data
) implements Serializable {
}