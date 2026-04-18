package com.arka.catalog.application.port.out.persistence;

import com.arka.catalog.domain.catalogoffer.entity.Price;
import com.arka.catalog.domain.catalogoffer.enumtype.PriceType;
import com.arka.catalog.domain.catalogoffer.valueobject.PriceId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogPricePersistencePort {

    Mono<Price> create(Price price);

    Mono<Price> update(Price price);

    Mono<Price> findById(TenantId tenantId, PriceId priceId);

    Flux<Price> findTimeline(TenantId tenantId, VariantId variantId, String currency, PriceType priceType);

    Mono<Price> resolveActive(TenantId tenantId, VariantId variantId, String currency, PriceType priceType, Instant at);
}
