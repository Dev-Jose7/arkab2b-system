package com.arka.notification.application.query;

public record GetNotificationAuditQuery(
        String tenantId,
        String targetType,
        String targetId,
        int page,
        int size) {
}
