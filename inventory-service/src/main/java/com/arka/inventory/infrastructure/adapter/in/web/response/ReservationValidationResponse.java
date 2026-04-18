package com.arka.inventory.infrastructure.adapter.in.web.response;

public record ReservationValidationResponse(
        String reservationId,
        String sku,
        int qty,
        boolean reservationConfirmed,
        boolean commitableAvailable) {}
