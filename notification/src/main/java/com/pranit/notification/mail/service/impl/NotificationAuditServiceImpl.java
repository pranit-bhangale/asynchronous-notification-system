package com.pranit.notification.mail.service.impl;

import com.pranit.notification.mail.event.EmailEvent;
import com.pranit.notification.mail.repository.NotificationAuditRepository;
import com.pranit.notification.mail.service.NotificationAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAuditServiceImpl implements NotificationAuditService {

    private final NotificationAuditRepository repository;

    @Override
    public void recordStart(final EmailEvent event, final String threadName) {

    }

    @Override
    public void recordFinishSuccess(final String eventId, final String format) {

    }

    @Override
    public void recordFinishFailure(final String eventId, final String unsupportedPurpose, final String errorMsg) {

    }

    @Override
    public void recordFinishFailure(final String eventId, final String simpleName, final String message, final String detailMessage) {

    }
}
