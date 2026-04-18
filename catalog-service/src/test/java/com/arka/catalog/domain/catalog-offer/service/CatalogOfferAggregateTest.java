package com.arka.catalog.domain.catalogoffer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.catalog.domain.catalogoffer.aggregate.CatalogOffer;
import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceType;
import com.arka.catalog.domain.catalogoffer.exception.VariantNotSellableException;
import com.arka.catalog.domain.catalogoffer.valueobject.Money;
import com.arka.catalog.domain.catalogoffer.valueobject.PriceId;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.TimeWindow;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CatalogOfferAggregateTest {

    @Test
    void shouldPublishConsistentCatalogOfferAndEmitDomainEvent() {
        Instant now = Instant.parse("2026-03-01T00:00:00Z");
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

        Variant variant = Variant.draft(
                TenantId.of("tenant-demo"),
                VariantId.of("variant-1"),
                product.productId(),
                "SKU-100",
                "Variant 1",
                "Description",
                100,
                now);
        variant.markSellable(product.status(), true, now.minusSeconds(1), null, now);

        Price price = Price.register(
                TenantId.of("tenant-demo"),
                PriceId.of("price-1"),
                variant.variantId(),
                PriceType.BASE,
                Money.of(new BigDecimal("12.50"), "COP"),
                TimeWindow.of(now.minusSeconds(1), now.plusSeconds(3600)),
                now);

        CatalogOffer offer = CatalogOffer.publish(product, variant, price, "policy-ref-1", now);

        assertEquals(variant.variantId().value(), offer.offerId().value());
        assertEquals(1, offer.pullDomainEvents().size());
    }

    @Test
    void shouldRejectPublishingWhenVariantIsNotSellableAtGivenTime() {
        Instant now = Instant.parse("2026-03-01T00:00:00Z");
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

        Variant variant = Variant.draft(
                TenantId.of("tenant-demo"),
                VariantId.of("variant-1"),
                product.productId(),
                "SKU-100",
                "Variant 1",
                "Description",
                100,
                now);

        Price price = Price.register(
                TenantId.of("tenant-demo"),
                PriceId.of("price-1"),
                variant.variantId(),
                PriceType.BASE,
                Money.of(new BigDecimal("12.50"), "COP"),
                TimeWindow.of(now.minusSeconds(1), now.plusSeconds(3600)),
                now);

        assertThrows(VariantNotSellableException.class, () -> CatalogOffer.publish(product, variant, price, "policy-ref-1", now));
    }
}
