package com.arka.order.application.port.out.directory;

import reactor.core.publisher.Mono;

public interface DirectoryCheckoutPort {

    Mono<DirectoryCheckoutContext> resolveCheckoutContext(
            String tenantId,
            String organizationId,
            String addressId,
            String countryCode);
}
