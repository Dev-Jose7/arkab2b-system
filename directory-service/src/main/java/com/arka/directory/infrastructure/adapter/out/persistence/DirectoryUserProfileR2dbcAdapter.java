package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.application.port.out.persistence.DirectoryUserProfilePersistencePort;
import com.arka.directory.domain.organizationcontext.entity.OrganizationUserProfile;
import com.arka.directory.infrastructure.adapter.out.persistence.mapper.DirectoryRowMapper;
import com.arka.directory.infrastructure.adapter.out.persistence.repository.ReactiveOrganizationUserProfileRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DirectoryUserProfileR2dbcAdapter implements DirectoryUserProfilePersistencePort {

    private final ReactiveOrganizationUserProfileRepository repository;
    private final DirectoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public DirectoryUserProfileR2dbcAdapter(
            ReactiveOrganizationUserProfileRepository repository,
            DirectoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<OrganizationUserProfile> upsert(OrganizationUserProfile userProfile) {
        var row = rowMapper.toRow(userProfile);
        return repository.existsById(row.userProfileId())
                .flatMap(exists -> exists ? repository.save(row) : entityTemplate.insert(row))
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<OrganizationUserProfile> findByOrganizationAndIamUserId(String organizationId, String iamUserId) {
        return repository.findByOrganizationIdAndIamUserId(organizationId, iamUserId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<OrganizationUserProfile> findByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId).map(rowMapper::toDomain);
    }
}
