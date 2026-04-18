package com.arka.catalog.application.result;

public record VariantAttributeResult(
        String attributeCode,
        String value,
        String normalizedValue) {
}
