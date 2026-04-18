package com.arka.notification.application.command;

public record RetryNotificationCommand(
        String tenantId,
        String actorId,
        String notificationId,
        String idempotencyKey) {
}
