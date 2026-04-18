package com.arka.catalog.domain.catalogoffer.event;

import java.time.Instant;

public final class CatalogOfferPublished extends AbstractCatalogDomainEvent {

    private final String organizationId;
    private final String productId;
    private final String variantId;
    private final String priceId;
    private final String regionalPolicyReference;

    public CatalogOfferPublished(
            String offerId,
            String organizationId,
            String productId,
            String variantId,
            String priceId,
            String regionalPolicyReference,
            Instant occurredAt) {
        super(offerId, "CatalogOffer", occurredAt);
        this.organizationId = organizationId;
        this.productId = productId;
        this.variantId = variantId;
        this.priceId = priceId;
        this.regionalPolicyReference = regionalPolicyReference;
    }

    @Override
    public String eventType() {
        return "CatalogOfferPublished";
    }

    public String organizationId() {
        return organizationId;
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
