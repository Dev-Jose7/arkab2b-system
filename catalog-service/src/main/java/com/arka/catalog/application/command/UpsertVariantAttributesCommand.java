package com.arka.catalog.application.command;

import java.util.List;

public record UpsertVariantAttributesCommand(
        String tenantId,
        String actorId,
        String variantId,
        List<VariantAttributeInput> attributes,
        String idempotencyKey) {
}
