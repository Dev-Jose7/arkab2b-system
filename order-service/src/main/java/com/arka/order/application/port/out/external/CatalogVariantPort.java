package com.arka.order.application.port.out.external;

import reactor.core.publisher.Mono;

public interface CatalogVariantPort {

    Mono<CatalogVariantSnapshot> resolveVariant(String tenantId, String variantId, String sku);
}
