package com.arka.directory.application.port.out.persistence;

import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import reactor.core.publisher.Mono;

public interface DirectoryOrganizationPersistencePort {

    Mono<Boolean> existsByCode(String organizationCode);

    Mono<Organization> save(Organization organization);

    Mono<Organization> findById(String organizationId);

    Mono<Long> countAll();

    Mono<Long> countByStatus(String status);
}
