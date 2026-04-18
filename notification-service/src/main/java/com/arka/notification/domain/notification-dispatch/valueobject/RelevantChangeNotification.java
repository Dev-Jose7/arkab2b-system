package com.arka.notification.domain.notificationdispatch.valueobject;

import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.shared.exception.DomainInvariantViolationException;

public record RelevantChangeNotification(
        TenantId tenantId,
        String sourceEventId,
        String sourceEventType,
        String recipientRef,
        NotificationChannel channel,
        String payloadJson,
        String traceId,
        String correlationId) {

    public RelevantChangeNotification {
        if (sourceEventId == null || sourceEventId.isBlank()) {
            throw new DomainInvariantViolationException("notificacion_relevante_invalida", "sourceEventId es obligatorio");
        }
        if (sourceEventType == null || sourceEventType.isBlank()) {
            throw new DomainInvariantViolationException("notificacion_relevante_invalida", "sourceEventType es obligatorio");
        }
        if (recipientRef == null || recipientRef.isBlank()) {
            throw new DomainInvariantViolationException("notificacion_relevante_invalida", "recipientRef es obligatorio");
        }
        payloadJson = payloadJson == null ? "{}" : payloadJson.trim();
        traceId = traceId == null ? "" : traceId.trim();
        correlationId = correlationId == null ? "" : correlationId.trim();
    }
}
