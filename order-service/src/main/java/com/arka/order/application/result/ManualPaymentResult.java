package com.arka.order.application.result;

import java.math.BigDecimal;
import java.time.Instant;

public record ManualPaymentResult(
        String paymentRecordId,
        String paymentReference,
        BigDecimal amount,
        String method,
        String supportReference,
        String status,
        Instant receivedAt,
        Instant createdAt,
        Instant updatedAt) {}
