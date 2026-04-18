package com.arka.catalog.domain.catalogoffer.repository;

import com.arka.catalog.domain.catalogoffer.aggregate.CatalogOffer;
import com.arka.catalog.domain.catalogoffer.valueobject.OfferId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import reactor.core.publisher.Mono;

public interface CatalogOfferRepository {

    Mono<CatalogOffer> save(CatalogOffer offer);

    Mono<CatalogOffer> findByOfferId(TenantId tenantId, OfferId offerId);

    Mono<CatalogOffer> findByVariantId(TenantId tenantId, String variantId);
}
