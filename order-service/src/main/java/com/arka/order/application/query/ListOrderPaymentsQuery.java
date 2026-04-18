package com.arka.order.application.query;

public record ListOrderPaymentsQuery(
        String organizationId,

        String orderId,
        String actorUserId) {}
