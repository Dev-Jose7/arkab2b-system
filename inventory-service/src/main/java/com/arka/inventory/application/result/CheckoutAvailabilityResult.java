package com.arka.inventory.application.result;

public record CheckoutAvailabilityResult(
        String stockItemId,
        int requestedQty,
        int availableQty,
        boolean reservable) {}
