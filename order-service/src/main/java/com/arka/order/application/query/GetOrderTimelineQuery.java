package com.arka.order.application.query;

public record GetOrderTimelineQuery(
        String organizationId,

        String orderId,
        String actorUserId) {}
