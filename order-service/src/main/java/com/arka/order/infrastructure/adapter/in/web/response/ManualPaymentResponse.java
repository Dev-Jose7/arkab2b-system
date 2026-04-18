package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ManualPaymentResponse(
        String paymentRecordId,
        String paymentReference,
        BigDecimal amount,
        String method,
        String supportReference,
        String status,
        Instant receivedAt,
        Instant createdAt,
        Instant updatedAt) {}
