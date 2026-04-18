package com.arka.notification.application.port.out.persistence;

public record NotificationSearchFilter(
        String organizationId,
        String status,
        String sourceEventType,
        String channel,
        String recipientRef,
        int offset,
        int limit) {
}
