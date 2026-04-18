package com.arka.order.application.query;

public record GetCheckoutAttemptByCorrelationQuery(
        String organizationId,

        String checkoutCorrelationId,
        String actorUserId) {}
