package com.arka.inventory.application.command;

public record ReleaseReservationCommand(
        String tenantId,
        String reservationId,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
