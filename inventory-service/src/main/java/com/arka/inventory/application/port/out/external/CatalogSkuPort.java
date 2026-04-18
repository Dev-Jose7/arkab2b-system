package com.arka.inventory.application.port.out.external;

import reactor.core.publisher.Mono;

public interface CatalogSkuPort {

    Mono<Boolean> existsSellableSku(String tenantId, String sku);
}
