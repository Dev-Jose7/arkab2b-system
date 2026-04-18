package com.arka.order.application.query;

public record GetOrderQuery(
        String tenantId,
        String organizationId,
        String orderId,
        String actorUserId) {}
