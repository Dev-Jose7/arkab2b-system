package com.arka.notification.application.command;

public record DiscardNotificationCommand(
        String organizationId,
        String actorId,
        String notificationId,
        String reason,
        String idempotencyKey) {
}
