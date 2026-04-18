package com.arka.inventory.application.result;

import java.time.Instant;

public record WarehouseResult(
        String warehouseId,
        String organizationId,
        String warehouseCode,
        String warehouseName,
        String countryCode,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
