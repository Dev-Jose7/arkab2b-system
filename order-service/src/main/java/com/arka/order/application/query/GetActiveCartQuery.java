package com.arka.order.application.query;

public record GetActiveCartQuery(
        String tenantId,
        String organizationId,
        String userId,
        String actorUserId) {}
