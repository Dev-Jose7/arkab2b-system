package com.arka.catalog.domain.catalogoffer.entity;

public record Category(
        String categoryId,
        String organizationId,
        String name,
        boolean active) {
}
