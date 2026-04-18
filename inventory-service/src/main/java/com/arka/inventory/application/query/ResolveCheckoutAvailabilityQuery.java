package com.arka.inventory.application.query;

public record ResolveCheckoutAvailabilityQuery(
        String organizationId,
        String stockItemId,
        Integer requestedQty,
        String actorUserId) {}
