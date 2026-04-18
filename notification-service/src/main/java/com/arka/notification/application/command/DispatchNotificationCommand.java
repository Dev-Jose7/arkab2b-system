package com.arka.notification.application.command;

public record DispatchNotificationCommand(
        String tenantId,
        String actorId,
        String notificationId,
        String idempotencyKey) {
}
