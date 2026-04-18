package com.arka.order.application.query;

public record GetOrderFinancialStatusQuery(
        String organizationId,

        String orderId,
        String actorUserId) {}
