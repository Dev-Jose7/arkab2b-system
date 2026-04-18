package com.arka.notification.application.port.out.persistence;

import java.math.BigDecimal;

public record NotificationMetricsProjection(
        long pendingDispatchCount,
        BigDecimal deliverySuccessRate,
        BigDecimal discardRate,
        BigDecimal meanAttemptsToSuccess,
        BigDecimal providerTimeoutRate) {
}
