package com.pranit.notification.mail.service;

import com.pranit.notification.mail.dto.EmailRequest;

@FunctionalInterface
public interface MailSender {

    void send(EmailRequest request);
}
