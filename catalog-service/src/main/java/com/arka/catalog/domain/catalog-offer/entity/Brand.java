package com.arka.catalog.domain.catalogoffer.entity;

public record Brand(
        String brandId,
        String tenantId,
        String name,
        boolean active) {
}
