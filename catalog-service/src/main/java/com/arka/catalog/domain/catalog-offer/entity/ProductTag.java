package com.arka.catalog.domain.catalogoffer.entity;

import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;

public record ProductTag(String tagCode, String tagValue) {

    public ProductTag {
        if (tagCode == null || tagCode.isBlank()) {
            throw new DomainInvariantViolationException("tag_invalido", "tagCode es obligatorio");
        }
        if (tagValue == null || tagValue.isBlank()) {
            throw new DomainInvariantViolationException("tag_invalido", "tagValue es obligatorio");
        }
        tagCode = tagCode.trim().toLowerCase();
        tagValue = tagValue.trim();
    }
}
