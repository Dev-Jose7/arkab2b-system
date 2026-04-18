package com.arka.inventory.application.command;

public record CreateWarehouseCommand(
        String organizationId,
        String warehouseCode,
        String warehouseName,
        String countryCode,
        String actorUserId,
        String idempotencyKey) {}
