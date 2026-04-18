package com.arka.catalog.domain.catalogoffer.service;

import com.arka.catalog.domain.catalogoffer.entity.VariantAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CatalogOfferPolicy {

    private static final Set<String> REQUIRED_ATTRIBUTES = Set.of("color", "size");

    private CatalogOfferPolicy() {
    }

    public static boolean hasRequiredAttributes(List<VariantAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return false;
        }
        Set<String> available = new HashSet<>();
        for (VariantAttribute attribute : attributes) {
            available.add(attribute.attributeCode().trim().toLowerCase());
        }
        return available.containsAll(REQUIRED_ATTRIBUTES);
    }
}
