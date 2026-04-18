package com.arka.directory.application.port.out.persistence;

import com.arka.directory.domain.organizationcontext.entity.OrganizationUserProfile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DirectoryUserProfilePersistencePort {

    Mono<OrganizationUserProfile> upsert(OrganizationUserProfile userProfile);

    Mono<OrganizationUserProfile> findByOrganizationAndIamUserId(String organizationId, String iamUserId);

    Flux<OrganizationUserProfile> findByOrganizationId(String organizationId);
}
