package com.pranit.notification.mail.exception;

import com.pranit.notification.shared.exception.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NonRetryableMailException extends BaseException {
    public NonRetryableMailException(String message) {
        super(message);
    }
}
