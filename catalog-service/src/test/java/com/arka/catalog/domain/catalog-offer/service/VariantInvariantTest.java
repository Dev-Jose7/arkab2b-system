package com.arka.catalog.domain.catalogoffer.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.enumtype.ProductStatus;
import com.arka.catalog.domain.catalogoffer.exception.ProductNotActiveException;
import com.arka.catalog.domain.catalogoffer.exception.RequiredAttributesMissingException;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class VariantInvariantTest {

    @Test
    void shouldRequireActiveProductBeforeMarkingVariantSellable() {
        Instant now = Instant.parse("2026-02-01T00:00:00Z");
        Variant variant = Variant.draft(
                TenantId.of("tenant-demo"),
                VariantId.of("variant-1"),
                ProductId.of("product-1"),
                "SKU-001",
                "Variant",
                "Description",
                100,
                now);

        assertThrows(
                ProductNotActiveException.class,
                () -> variant.markSellable(ProductStatus.DRAFT, true, now, null, now));
    }

    @Test
    void shouldRequireMandatoryAttributesBeforeMarkingVariantSellable() {
        Instant now = Instant.parse("2026-02-01T00:00:00Z");
        Variant variant = Variant.draft(
                TenantId.of("tenant-demo"),
                VariantId.of("variant-2"),
                ProductId.of("product-1"),
                "SKU-002",
                "Variant",
                "Description",
                100,
                now);

        assertThrows(
                RequiredAttributesMissingException.class,
                () -> variant.markSellable(ProductStatus.ACTIVE, false, now, null, now));
    }
}
