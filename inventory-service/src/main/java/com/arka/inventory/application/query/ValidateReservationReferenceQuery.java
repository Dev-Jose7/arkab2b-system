package com.arka.inventory.application.query;

public record ValidateReservationReferenceQuery(
        String tenantId,
        String reservationId,
        String sku,
        int qty) {}
