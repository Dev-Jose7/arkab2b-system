package com.arka.inventory.application.query;

public record ValidateReservationReferenceQuery(
        String organizationId,
        String reservationId,
        String sku,
        int qty) {}
