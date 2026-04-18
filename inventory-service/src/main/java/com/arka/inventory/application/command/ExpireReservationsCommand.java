package com.arka.inventory.application.command;

public record ExpireReservationsCommand(
        String organizationId,
        Integer batchSize,
        String actorUserId) {}
