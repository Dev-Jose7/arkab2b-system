package com.arka.catalog.infrastructure.adapter.out.persistence;

import com.arka.catalog.application.port.out.persistence.CatalogOfferPersistencePort;
import com.arka.catalog.domain.catalogoffer.aggregate.CatalogOffer;
import com.arka.catalog.domain.catalogoffer.repository.CatalogOfferRepository;
import com.arka.catalog.domain.catalogoffer.valueobject.OfferId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DomainCatalogOfferRepositoryAdapter implements CatalogOfferRepository {

    private final CatalogOfferPersistencePort persistencePort;

    public DomainCatalogOfferRepositoryAdapter(CatalogOfferPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Mono<CatalogOffer> save(CatalogOffer offer) {
        return persistencePort
                .findByOfferId(offer.tenantId(), offer.offerId())
                .flatMap(existing -> persistencePort.update(offer))
                .switchIfEmpty(persistencePort.create(offer));
    }

    @Override
    public Mono<CatalogOffer> findByOfferId(TenantId tenantId, OfferId offerId) {
        return persistencePort.findByOfferId(tenantId, offerId);
    }

    @Override
    public Mono<CatalogOffer> findByVariantId(TenantId tenantId, String variantId) {
        return persistencePort.findByVariantId(tenantId, VariantId.of(variantId));
    }
}
