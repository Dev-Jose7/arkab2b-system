package com.arka.order.application.query;

public record GetOrderQuery(
        String organizationId,

        String orderId,
        String actorUserId) {}
