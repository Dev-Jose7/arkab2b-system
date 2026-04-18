package com.arka.catalog.domain.catalogoffer.entity;

public record Category(
        String categoryId,
        String tenantId,
        String name,
        boolean active) {
}
