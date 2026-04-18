package com.arka.notification.application.port.out.external;

public record ProviderSendResult(
        boolean success,
        String providerRef,
        String errorCode,
        String errorMessage,
        long latencyMs,
        boolean retryable,
        String rawResponse) {
}
