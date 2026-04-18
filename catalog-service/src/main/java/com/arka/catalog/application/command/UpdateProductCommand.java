package com.arka.catalog.application.command;

import java.util.List;

public record UpdateProductCommand(
        String organizationId,
        String actorId,
        String productId,
        String name,
        String description,
        String brandId,
        String categoryId,
        List<String> tags,
        String idempotencyKey) {
}
