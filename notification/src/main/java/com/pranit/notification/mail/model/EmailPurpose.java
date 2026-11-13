package com.pranit.notification.mail.model;

/**
 * Enumeration representing the business purpose/intent of an email event.
 * Used by {@link com.pranit.notification.mail.router.EmailRouter} to route to corresponding handlers.
 */
public enum EmailPurpose {
    WELCOME,
    PASSWORD_RESET,
    EMAIL_VERIFICATION,
    ORDER_CONFIRMATION,
    NOTIFICATION,
    ALERT,
    GENERIC
}
