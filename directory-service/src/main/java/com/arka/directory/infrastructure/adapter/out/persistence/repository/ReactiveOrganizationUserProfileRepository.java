package com.arka.directory.infrastructure.adapter.out.persistence.repository;

import com.arka.directory.infrastructure.adapter.out.persistence.entity.OrganizationUserProfileRow;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveOrganizationUserProfileRepository extends ReactiveCrudRepository<OrganizationUserProfileRow, String> {

    Mono<OrganizationUserProfileRow> findByOrganizationIdAndIamUserId(String organizationId, String iamUserId);

    Flux<OrganizationUserProfileRow> findByOrganizationId(String organizationId);
}
