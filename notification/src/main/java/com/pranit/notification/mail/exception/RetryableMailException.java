package com.pranit.notification.mail.exception;

import com.pranit.notification.shared.exception.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class RetryableMailException extends BaseException {
    public RetryableMailException(String message) {
        super(message);
    }
}
