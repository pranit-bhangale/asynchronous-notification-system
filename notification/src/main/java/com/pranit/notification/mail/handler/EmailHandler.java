package com.pranit.notification.mail.handler;


import com.pranit.notification.mail.model.EmailPurpose;

public interface EmailHandler<T> {

    EmailPurpose supports();

    void send(T event);
}