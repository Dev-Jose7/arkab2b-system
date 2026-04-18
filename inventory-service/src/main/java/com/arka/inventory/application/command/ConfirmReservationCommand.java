package com.arka.inventory.application.command;

public record ConfirmReservationCommand(
        String tenantId,
        String reservationId,
        String orderId,
        String actorUserId,
        String idempotencyKey) {}
