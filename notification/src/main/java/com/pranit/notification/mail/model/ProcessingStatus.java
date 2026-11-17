package com.pranit.notification.mail.model;

/**
 * Lifecycle statuses for auditing email processing and dispatch stages.
 */
public enum ProcessingStatus {
    START,
    VALIDATING,
    TEMPLATE_RENDER_START,
    TEMPLATE_RENDER_SUCCESS,
    DISPATCHING,
    SENT,
    RETRYING,
    FAILED,
    DEAD_LETTER
}
