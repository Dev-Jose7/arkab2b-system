package com.arka.notification.application.command;

public record DiscardNotificationCommand(
        String tenantId,
        String actorId,
        String notificationId,
        String reason,
        String idempotencyKey) {
}
