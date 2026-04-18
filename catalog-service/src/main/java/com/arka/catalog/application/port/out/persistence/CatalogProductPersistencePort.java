package com.arka.catalog.application.port.out.persistence;

import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.ProductTag;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogProductPersistencePort {

    Mono<Product> create(Product product, Iterable<ProductTag> tags);

    Mono<Product> update(Product product, Iterable<ProductTag> tags);

    Mono<Product> findById(TenantId tenantId, ProductId productId);

    Mono<Boolean> existsByProductCode(TenantId tenantId, String productCode, String excludingProductId);

    Flux<ProductTag> findTags(TenantId tenantId, ProductId productId);
}
