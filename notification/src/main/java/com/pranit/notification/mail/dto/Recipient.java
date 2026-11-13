package com.pranit.notification.mail.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Encapsulates destination email addresses for a notification request.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Recipient(
        @NotBlank(message = "Primary recipient email ('to') cannot be blank")
        @Email(message = "Primary recipient email ('to') must be a valid email format")
        @JsonProperty("to")
        String to,

        @JsonProperty("name")
        String name,

        @JsonProperty("cc")
        List<@Email(message = "Invalid CC email format") String> cc,

        @JsonProperty("bcc")
        List<@Email(message = "Invalid BCC email format") String> bcc
) implements Serializable {

    /**
     * Defensive copy constructor to ensure lists are non-null and immutable
     * when passed across asynchronous virtual threads.
     */
    public Recipient {
        cc = cc != null ? List.copyOf(cc) : List.of();
        bcc = bcc != null ? List.copyOf(bcc) : List.of();
    }

    public static Recipient of(String to) {
        return new Recipient(to, null, List.of(), List.of());
    }

    public static Recipient of(String to, String name) {
        return new Recipient(to, name, List.of(), List.of());
    }

    public String getTo() {
        return to;
    }

    public String getName() {
        return name;
    }

    public List<String> getCc() {
        return cc;
    }

    public List<String> getBcc() {
        return bcc;
    }
}
