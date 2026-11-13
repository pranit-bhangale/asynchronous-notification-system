package com.pranit.notification.mail.event;

import com.pranit.notification.mail.model.EmailPurpose;

/**
 * Common contract for all email notification events across the system.
 */
public interface EmailEvent {

    String getEventId();

    String getRecipientEmail();

    EmailPurpose getPurpose();
}
