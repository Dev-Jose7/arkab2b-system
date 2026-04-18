package com.arka.catalog.application.command;

import java.util.List;

public record CreateProductCommand(
        String tenantId,
        String actorId,
        String productCode,
        String name,
        String description,
        String brandId,
        String categoryId,
        List<String> tags,
        String idempotencyKey) {
}
