package com.arka.notification.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record NotificationMetricsResponse(
        long pendingDispatchCount,
        BigDecimal deliverySuccessRate,
        BigDecimal discardRate,
        BigDecimal meanAttemptsToSuccess,
        BigDecimal providerTimeoutRate) {
}
