package com.arka.order.application.query;

public record CalculateOrderAmountsQuery(
        String tenantId,
        String organizationId,
        String orderId,
        String actorUserId) {}
