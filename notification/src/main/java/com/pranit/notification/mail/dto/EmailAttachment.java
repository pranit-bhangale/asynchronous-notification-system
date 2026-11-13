package com.pranit.notification.mail.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.io.Serializable;

/**
 * Metadata and payload representation for email attachments.
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

        @JsonProperty("data")
        byte[] data,

        @JsonProperty("downloadUrl")
        String downloadUrl,

        @JsonProperty("inline")
        boolean inline,

        @JsonProperty("contentId")
        String contentId
) implements Serializable {

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public boolean isInline() {
        return inline;
    }

    public String getContentId() {
        return contentId;
    }
}
