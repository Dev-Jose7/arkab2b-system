package com.arka.notification.application.exception;

public class NotificationResourceNotFoundException extends ApplicationException {

    public NotificationResourceNotFoundException(String resourceName, String id) {
        super("notification_recurso_no_encontrado", resourceName + " no encontrado: " + id);
    }
}
