package com.pranit.notification.mail.service;

import com.pranit.notification.mail.event.EmailEvent;

public interface NotificationAuditService {

    void recordStart(EmailEvent event, String threadName);

    void recordFinishSuccess(String eventId, String format);

    void recordFinishFailure(String eventId, String unsupportedPurpose, String errorMsg);

    void recordFinishFailure(String eventId, String simpleName, String message, String detailMessage);
}
