package com.arka.catalog.domain.catalogoffer.enumtype;

public enum VariantStatus {
    DRAFT,
    SELLABLE,
    DISCONTINUED;

    public boolean isSellable() {
        return this == SELLABLE;
    }
}
