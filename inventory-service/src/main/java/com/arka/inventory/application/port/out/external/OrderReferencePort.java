package com.arka.inventory.application.port.out.external;

import reactor.core.publisher.Mono;

public interface OrderReferencePort {

    Mono<Boolean> isValidCartReference(String organizationId, String cartId);

    Mono<Boolean> isValidOrderReference(String organizationId, String orderId);
}
