package com.arka.catalog.application.port.out.persistence;

import com.arka.catalog.domain.catalogoffer.entity.Variant;
import com.arka.catalog.domain.catalogoffer.entity.VariantAttribute;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import com.arka.catalog.domain.catalogoffer.valueobject.VariantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogVariantPersistencePort {

    Mono<Variant> create(Variant variant, Iterable<VariantAttribute> attributes);

    Mono<Variant> update(Variant variant);

    Mono<Variant> findById(TenantId tenantId, VariantId variantId);

    Mono<Variant> findBySku(TenantId tenantId, String sku);

    Flux<Variant> findByProductId(TenantId tenantId, ProductId productId);

    Flux<VariantAttribute> findAttributes(TenantId tenantId, VariantId variantId);

    Mono<Void> replaceAttributes(TenantId tenantId, VariantId variantId, Iterable<VariantAttribute> attributes);

    Mono<Boolean> existsSellableSku(TenantId tenantId, String sku, String excludingVariantId);
}
