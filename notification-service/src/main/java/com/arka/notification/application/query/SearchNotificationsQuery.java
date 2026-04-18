package com.arka.notification.application.query;

public record SearchNotificationsQuery(
        String organizationId,
        String status,
        String sourceEventType,
        String channel,
        String recipientRef,
        int page,
        int size) {
}
