package com.arka.inventory.application.command;

public record ExpireReservationsCommand(
        String tenantId,
        Integer batchSize,
        String actorUserId) {}
