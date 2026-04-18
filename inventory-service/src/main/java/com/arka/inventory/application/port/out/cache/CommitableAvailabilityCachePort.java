package com.arka.inventory.application.port.out.cache;

import com.arka.inventory.application.result.CommitableAvailabilityResult;
import reactor.core.publisher.Mono;

public interface CommitableAvailabilityCachePort {

    Mono<CommitableAvailabilityResult> find(String tenantId, String warehouseId, String sku);

    Mono<Void> put(CommitableAvailabilityResult availabilityResult);

    Mono<Void> evict(String tenantId, String warehouseId, String sku);
}
