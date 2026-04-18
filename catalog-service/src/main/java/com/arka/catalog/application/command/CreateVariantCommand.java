package com.arka.catalog.application.command;

import java.util.List;

public record CreateVariantCommand(
        String organizationId,
        String actorId,
        String productId,
        String sku,
        String name,
        String description,
        Integer weightGrams,
        List<VariantAttributeInput> attributes,
        String idempotencyKey) {
}
