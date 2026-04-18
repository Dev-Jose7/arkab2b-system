package com.arka.order.application.query;

public record GetActiveCartQuery(
        String organizationId,

        String userId,
        String actorUserId) {}
