package com.arka.catalog.application.port.out.persistence;

import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.entity.VariantAttribute;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.OrganizationId;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogVariantPersistencePort {

    Mono<Variant> create(Variant variant, Iterable<VariantAttribute> attributes);

    Mono<Variant> update(Variant variant);

    Mono<Variant> findById(OrganizationId organizationId, VariantId variantId);

    Mono<Variant> findBySku(OrganizationId organizationId, String sku);

    Flux<Variant> findByProductId(OrganizationId organizationId, ProductId productId);

    Flux<VariantAttribute> findAttributes(OrganizationId organizationId, VariantId variantId);

    Mono<Void> replaceAttributes(OrganizationId organizationId, VariantId variantId, Iterable<VariantAttribute> attributes);

    Mono<Boolean> existsSellableSku(OrganizationId organizationId, String sku, String excludingVariantId);
}
