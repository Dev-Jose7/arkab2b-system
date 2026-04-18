package com.arka.directory.application.port.out.persistence;

import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DirectoryCountryPolicyPersistencePort {

    Mono<CountryPolicy> save(CountryPolicy countryPolicy);

    Mono<CountryPolicy> findActiveByOrganizationAndCountry(String organizationId, String countryCode);

    Mono<CountryPolicy> findLatestByOrganizationAndCountry(String organizationId, String countryCode);

    Flux<CountryPolicy> findActiveByOrganization(String organizationId);

    Mono<Void> supersedeActiveByOrganizationAndCountry(String organizationId, String countryCode);

    Mono<Long> countActive();
}
