package com.arka.order.application.query;

public record ListOrderPaymentsQuery(
        String tenantId,
        String organizationId,
        String orderId,
        String actorUserId) {}
