package com.arka.notification.application.command;

public record EmitRelevantChangeNotificationCommand(
        String tenantId,
        String actorId,
        String sourceEventId,
        String sourceEventType,
        String recipientRef,
        String channel,
        String payloadJson,
        String traceId,
        String correlationId,
        String idempotencyKey) {
}
