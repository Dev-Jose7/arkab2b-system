package com.arka.inventory.application.query;

public record ResolveCheckoutAvailabilityQuery(
        String tenantId,
        String stockItemId,
        Integer requestedQty,
        String actorUserId) {}
