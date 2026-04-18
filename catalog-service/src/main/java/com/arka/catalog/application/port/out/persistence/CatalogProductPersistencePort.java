package com.arka.catalog.application.port.out.persistence;

import com.arka.catalog.domain.catalogoffer.entity.Product;
import com.arka.catalog.domain.catalogoffer.entity.ProductTag;
import com.arka.catalog.domain.catalogoffer.valueobject.ProductId;
import com.arka.catalog.domain.catalogoffer.valueobject.OrganizationId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogProductPersistencePort {

    Mono<Product> create(Product product, Iterable<ProductTag> tags);

    Mono<Product> update(Product product, Iterable<ProductTag> tags);

    Mono<Product> findById(OrganizationId organizationId, ProductId productId);

    Mono<Boolean> existsByProductCode(OrganizationId organizationId, String productCode, String excludingProductId);

    Flux<ProductTag> findTags(OrganizationId organizationId, ProductId productId);
}
