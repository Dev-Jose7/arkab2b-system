package com.arka.catalog.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.VariantAttribute;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.infrastructure.adapter.out.persistence.mapper.CatalogRowMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CatalogRowMapperTest {

    private final CatalogRowMapper mapper = new CatalogRowMapper();

    @Test
    void shouldRoundTripProductBetweenDomainAndRow() {
        Instant now = Instant.parse("2026-04-10T10:00:00Z");
        Product product = Product.draft(
                TenantId.of("tenant-demo"),
                ProductId.of("product-1"),
                "PROD-1",
                "Product 1",
                "Description",
                "brand-acme",
                "category-beverages",
                now);
        product.activate(now);

        Product mapped = mapper.toDomain(mapper.toRow(product));

        assertEquals(product.productId().value(), mapped.productId().value());
        assertEquals(product.tenantId().value(), mapped.tenantId().value());
        assertEquals(product.productCode(), mapped.productCode());
        assertEquals(product.status(), mapped.status());
    }

    @Test
    void shouldMapVariantAttributeToPersistenceRow() {
        Instant now = Instant.parse("2026-04-10T10:00:00Z");
        VariantAttribute attribute = new VariantAttribute("color", "Red", "red");

        var row = mapper.toRow("tenant-demo", "variant-1", attribute, now);

        assertEquals("tenant-demo", row.tenantId());
        assertEquals("variant-1", row.variantId());
        assertEquals("color", row.attributeCode());
        assertEquals("Red", row.attributeValue());
        assertEquals("red", row.normalizedValue());
    }
}
