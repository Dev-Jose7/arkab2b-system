package com.arka.catalog.domain.catalogoffer.event;

import java.time.Instant;

public final class CatalogOfferPublished extends AbstractCatalogDomainEvent {

    private final String tenantId;
    private final String productId;
    private final String variantId;
    private final String priceId;
    private final String regionalPolicyReference;

    public CatalogOfferPublished(
            String offerId,
            String tenantId,
            String productId,
            String variantId,
            String priceId,
            String regionalPolicyReference,
            Instant occurredAt) {
        super(offerId, "CatalogOffer", occurredAt);
        this.tenantId = tenantId;
        this.productId = productId;
        this.variantId = variantId;
        this.priceId = priceId;
        this.regionalPolicyReference = regionalPolicyReference;
    }

    @Override
    public String eventType() {
        return "CatalogOfferPublished";
    }

    public String tenantId() {
        return tenantId;
    }

    public String productId() {
        return productId;
    }

    public String variantId() {
        return variantId;
    }

    public String priceId() {
        return priceId;
    }

    public String regionalPolicyReference() {
        return regionalPolicyReference;
    }
}
