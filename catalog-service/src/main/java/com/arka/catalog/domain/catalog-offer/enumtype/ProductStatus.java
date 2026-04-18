package com.arka.catalog.domain.catalogoffer.enumtype;

public enum ProductStatus {
    DRAFT,
    ACTIVE,
    RETIRED;

    public boolean isCommerciallyUsable() {
        return this == ACTIVE;
    }
}
