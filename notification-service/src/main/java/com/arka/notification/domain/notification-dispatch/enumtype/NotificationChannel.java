package com.arka.notification.domain.notificationdispatch.enumtype;

public enum NotificationChannel {
    EMAIL,
    SMS,
    WHATSAPP,
    IN_APP;

    public static NotificationChannel from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("channel es obligatorio");
        }
        return NotificationChannel.valueOf(rawValue.trim().toUpperCase());
    }
}
