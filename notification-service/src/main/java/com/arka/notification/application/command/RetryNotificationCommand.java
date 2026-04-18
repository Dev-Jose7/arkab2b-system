package com.arka.notification.application.command;

public record RetryNotificationCommand(
        String organizationId,
        String actorId,
        String notificationId,
        String idempotencyKey) {
}
