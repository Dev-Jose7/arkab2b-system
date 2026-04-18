package com.arka.catalog.application.command;

import java.util.List;

public record UpsertVariantAttributesCommand(
        String organizationId,
        String actorId,
        String variantId,
        List<VariantAttributeInput> attributes,
        String idempotencyKey) {
}
