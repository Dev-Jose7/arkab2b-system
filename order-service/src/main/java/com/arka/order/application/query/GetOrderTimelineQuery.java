package com.arka.order.application.query;

public record GetOrderTimelineQuery(
        String tenantId,
        String organizationId,
        String orderId,
        String actorUserId) {}
