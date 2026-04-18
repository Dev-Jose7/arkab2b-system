package com.arka.reporting.application.port.out.directory;

import reactor.core.publisher.Mono;

public interface RegionalPolicyPort {

    Mono<RegionalPolicyResolution> resolveForOperation(String tenantId, String countryCode);
}
