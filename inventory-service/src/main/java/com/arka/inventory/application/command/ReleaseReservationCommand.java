package com.arka.inventory.application.command;

public record ReleaseReservationCommand(
        String organizationId,
        String reservationId,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
