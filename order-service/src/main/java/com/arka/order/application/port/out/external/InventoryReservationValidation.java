package com.arka.order.application.port.out.external;

public record InventoryReservationValidation(
        String reservationId,
        String sku,
        int qty,
        boolean reservationConfirmed,
        boolean commitableAvailable) {}
