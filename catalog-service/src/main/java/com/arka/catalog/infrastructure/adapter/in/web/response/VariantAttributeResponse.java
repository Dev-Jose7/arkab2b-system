package com.arka.catalog.infrastructure.adapter.in.web.response;

public record VariantAttributeResponse(
        String attributeCode,
        String value,
        String normalizedValue) {
}
