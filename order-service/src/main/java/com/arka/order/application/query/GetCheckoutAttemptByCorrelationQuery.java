package com.arka.order.application.query;

public record GetCheckoutAttemptByCorrelationQuery(
        String tenantId,
        String organizationId,
        String checkoutCorrelationId,
        String actorUserId) {}
