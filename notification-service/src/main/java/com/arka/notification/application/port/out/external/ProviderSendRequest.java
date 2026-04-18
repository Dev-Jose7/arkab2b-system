package com.arka.notification.application.port.out.external;

public record ProviderSendRequest(
        String tenantId,
        String providerCode,
        String channel,
        String destination,
        String renderedPayload,
        String traceId,
        String correlationId) {
}
