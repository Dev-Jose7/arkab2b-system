package com.arka.inventory.application.command;

public record ConfirmReservationCommand(
        String organizationId,
        String reservationId,
        String orderId,
        String actorUserId,
        String idempotencyKey) {}
