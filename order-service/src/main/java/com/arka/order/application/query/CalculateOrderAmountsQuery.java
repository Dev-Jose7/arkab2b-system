package com.arka.order.application.query;

public record CalculateOrderAmountsQuery(
        String organizationId,

        String orderId,
        String actorUserId) {}
