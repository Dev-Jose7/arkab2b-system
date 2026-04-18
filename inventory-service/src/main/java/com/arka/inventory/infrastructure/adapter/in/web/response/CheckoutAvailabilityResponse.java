package com.arka.inventory.infrastructure.adapter.in.web.response;

public record CheckoutAvailabilityResponse(
        String stockItemId,
        int requestedQty,
        int availableQty,
        boolean reservable) {}
