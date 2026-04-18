package com.arka.directory.application.port.out.cache;

import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import reactor.core.publisher.Mono;

public interface DirectoryPolicyCachePort {

    Mono<CountryPolicy> findActive(String organizationId, String countryCode);

    Mono<Void> putActive(CountryPolicy countryPolicy);

    Mono<Void> evictActive(String organizationId, String countryCode);
}
