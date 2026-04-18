package com.arka.catalog.application.command;

import java.util.List;

public record UpdateProductCommand(
        String tenantId,
        String actorId,
        String productId,
        String name,
        String description,
        String brandId,
        String categoryId,
        List<String> tags,
        String idempotencyKey) {
}
