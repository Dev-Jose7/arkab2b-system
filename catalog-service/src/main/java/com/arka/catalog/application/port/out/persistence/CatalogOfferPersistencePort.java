package com.arka.catalog.application.port.out.persistence;

import com.arka.catalog.domain.catalogoffer.aggregate.CatalogOffer;
import com.arka.catalog.domain.catalogoffer.valueobject.OfferId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import reactor.core.publisher.Mono;

public interface CatalogOfferPersistencePort {

    Mono<CatalogOffer> create(CatalogOffer offer);

    Mono<CatalogOffer> update(CatalogOffer offer);

    Mono<CatalogOffer> findByOfferId(TenantId tenantId, OfferId offerId);

    Mono<CatalogOffer> findByVariantId(TenantId tenantId, VariantId variantId);
}
