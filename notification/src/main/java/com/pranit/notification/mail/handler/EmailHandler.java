package com.pranit.notification.mail.handler;


import com.pranit.notification.mail.dto.EmailRequest;
import com.pranit.notification.mail.model.EmailPurpose;

public interface EmailHandler {

    EmailPurpose supports();

    void send(EmailRequest request);
}