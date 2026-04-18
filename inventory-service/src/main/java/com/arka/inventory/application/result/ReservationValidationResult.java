package com.arka.inventory.application.result;

public record ReservationValidationResult(
        String reservationId,
        String sku,
        int qty,
        boolean reservationConfirmed,
        boolean commitableAvailable) {}
