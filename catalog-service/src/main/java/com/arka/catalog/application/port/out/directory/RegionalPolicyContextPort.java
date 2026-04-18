package com.arka.catalog.application.port.out.directory;

import reactor.core.publisher.Mono;

public interface RegionalPolicyContextPort {

    Mono<RegionalPolicyContext> resolveForTenant(String tenantId, String countryCode);
}
