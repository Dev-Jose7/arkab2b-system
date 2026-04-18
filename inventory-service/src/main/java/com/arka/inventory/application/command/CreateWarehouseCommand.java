package com.arka.inventory.application.command;

public record CreateWarehouseCommand(
        String tenantId,
        String warehouseCode,
        String warehouseName,
        String countryCode,
        String actorUserId,
        String idempotencyKey) {}
