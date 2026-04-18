package com.arka.inventory.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record WarehouseResponse(
        String warehouseId,
        String organizationId,
        String warehouseCode,
        String warehouseName,
        String countryCode,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
