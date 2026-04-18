package com.arka.order.application.command;

public record ValidateManualPaymentCommand(
        String tenantId,
        String organizationId,
        String orderId,
        String paymentRecordId,
        String targetStatus,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
