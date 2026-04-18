package com.arka.catalog.domain.catalogoffer.entity;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;

public record VariantAttribute(String attributeCode, String value, String normalizedValue) {

    public VariantAttribute {
        if (attributeCode == null || attributeCode.isBlank()) {
            throw new DomainInvariantViolationException("required_attributes_missing", "attributeCode es obligatorio");
        }
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("required_attributes_missing", "El valor del atributo es obligatorio");
        }
        attributeCode = attributeCode.trim().toLowerCase();
        value = value.trim();
        normalizedValue = normalizedValue == null ? value.toLowerCase() : normalizedValue.trim().toLowerCase();
    }
}
