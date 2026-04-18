package com.arka.catalog.domain.catalogoffer.entity;

public record Brand(
        String brandId,
        String organizationId,
        String name,
        boolean active) {
}
