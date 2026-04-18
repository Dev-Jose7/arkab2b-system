package com.arka.notification.application.result;

import java.math.BigDecimal;

public record NotificationMetricsResult(
        long pendingDispatchCount,
        BigDecimal deliverySuccessRate,
        BigDecimal discardRate,
        BigDecimal meanAttemptsToSuccess,
        BigDecimal providerTimeoutRate) {
}
