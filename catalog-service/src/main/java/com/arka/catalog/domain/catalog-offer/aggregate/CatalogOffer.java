package com.arka.catalog.domain.catalogoffer.aggregate;

import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.enumtype.ProductStatus;
import com.arka.catalog.domain.catalogoffer.event.CatalogOfferPublished;
import com.arka.catalog.domain.catalogoffer.event.CatalogOfferUpdated;
import com.arka.catalog.domain.catalogoffer.exception.ProductNotActiveException;
import com.arka.catalog.domain.catalogoffer.exception.VariantNotSellableException;
import com.arka.catalog.domain.catalogoffer.valueobject.OfferId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.shared.event.DomainEvent;
import com.arka.catalog.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class CatalogOffer {

    private final OfferId offerId;
    private final TenantId tenantId;
    private final Product product;
    private final List<DomainEvent> domainEvents;

    private Variant variant;
    private Price price;
    private String regionalPolicyReference;
    private Instant publishedAt;
    private Instant updatedAt;

    private CatalogOffer(
            OfferId offerId,
            TenantId tenantId,
            Product product,
            Variant variant,
            Price price,
            String regionalPolicyReference,
            Instant publishedAt,
            Instant updatedAt,
            List<DomainEvent> domainEvents) {
        this.offerId = offerId;
        this.tenantId = tenantId;
        this.product = product;
        this.variant = variant;
        this.price = price;
        this.regionalPolicyReference = regionalPolicyReference == null ? "" : regionalPolicyReference.trim();
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
        this.domainEvents = domainEvents;
        ensureConsistency(updatedAt);
    }

    public static CatalogOffer publish(
            Product product,
            Variant variant,
            Price price,
            String regionalPolicyReference,
            Instant now) {
        CatalogOffer offer = new CatalogOffer(
                OfferId.of(variant.variantId().value()),
                product.tenantId(),
                product,
                variant,
                price,
                regionalPolicyReference,
                now,
                now,
                new ArrayList<>());
        offer.domainEvents.add(new CatalogOfferPublished(
                offer.offerId.value(),
                offer.tenantId.value(),
                product.productId().value(),
                variant.variantId().value(),
                price.priceId().value(),
                offer.regionalPolicyReference,
                now));
        return offer;
    }

    public static CatalogOffer rehydrate(
            OfferId offerId,
            Product product,
            Variant variant,
            Price price,
            String regionalPolicyReference,
            Instant publishedAt,
            Instant updatedAt) {
        return new CatalogOffer(
                offerId,
                product.tenantId(),
                product,
                variant,
                price,
                regionalPolicyReference,
                publishedAt,
                updatedAt,
                new ArrayList<>());
    }

    public void updateOffer(
            Variant variant,
            Price price,
            String regionalPolicyReference,
            Instant now) {
        if (!this.variant.variantId().value().equals(variant.variantId().value())) {
            throw new DomainInvariantViolationException(
                    "oferta_inconsistente",
                    "No se puede cambiar variantId en una oferta publicada");
        }
        this.variant = variant;
        this.price = price;
        this.regionalPolicyReference = regionalPolicyReference == null ? "" : regionalPolicyReference.trim();
        this.updatedAt = now;
        ensureConsistency(now);
        this.domainEvents.add(new CatalogOfferUpdated(
                offerId.value(),
                tenantId.value(),
                product.productId().value(),
                variant.variantId().value(),
                price.priceId().value(),
                this.regionalPolicyReference,
                now));
    }

    private void ensureConsistency(Instant now) {
        if (product.status() != ProductStatus.ACTIVE) {
            throw new ProductNotActiveException();
        }
        if (!product.tenantId().value().equals(variant.tenantId().value())
                || !product.tenantId().value().equals(price.tenantId().value())) {
            throw new DomainInvariantViolationException(
                    "oferta_inconsistente",
                    "Producto, variante y precio deben pertenecer al mismo tenant");
        }
        if (!variant.productId().value().equals(product.productId().value())) {
            throw new DomainInvariantViolationException(
                    "oferta_inconsistente",
                    "La variante debe pertenecer al producto de la oferta");
        }
        if (!price.variantId().value().equals(variant.variantId().value())) {
            throw new DomainInvariantViolationException(
                    "oferta_inconsistente",
                    "El precio debe pertenecer a la variante publicada");
        }
        if (!variant.isSellableAt(now) || !price.isActiveAt(now)) {
            throw new VariantNotSellableException();
        }
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public OfferId offerId() {
        return offerId;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public Product product() {
        return product;
    }

    public Variant variant() {
        return variant;
    }

    public Price price() {
        return price;
    }

    public String regionalPolicyReference() {
        return regionalPolicyReference;
    }

    public Instant publishedAt() {
        return publishedAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
