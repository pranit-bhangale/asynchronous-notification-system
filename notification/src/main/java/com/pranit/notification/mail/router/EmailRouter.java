package com.pranit.notification.mail.router;

import com.pranit.notification.mail.event.EmailEvent;
import com.pranit.notification.mail.model.EmailPurpose;

@FunctionalInterface
public interface EmailRouter {

    void send(EmailEvent event, EmailPurpose purpose);
}

