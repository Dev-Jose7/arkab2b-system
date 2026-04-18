package com.arka.notification.domain.notificationdispatch.exception;

import com.arka.notification.domain.shared.exception.DomainException;

public class NotificationDomainException extends DomainException {

    public NotificationDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}
