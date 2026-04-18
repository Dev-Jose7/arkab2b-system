package com.arka.order.application.command;

import java.math.BigDecimal;
import java.time.Instant;

public record RegisterManualPaymentCommand(
        String tenantId,
        String organizationId,
        String orderId,
        String paymentReference,
        BigDecimal amount,
        String method,
        String supportReference,
        Instant receivedAt,
        String actorUserId,
        String idempotencyKey) {}
