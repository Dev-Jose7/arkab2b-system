package com.arka.notification.application.query;

public record GetNotificationAuditQuery(
        String organizationId,
        String targetType,
        String targetId,
        int page,
        int size) {
}
