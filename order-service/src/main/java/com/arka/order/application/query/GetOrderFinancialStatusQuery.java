package com.arka.order.application.query;

public record GetOrderFinancialStatusQuery(
        String tenantId,
        String organizationId,
        String orderId,
        String actorUserId) {}
