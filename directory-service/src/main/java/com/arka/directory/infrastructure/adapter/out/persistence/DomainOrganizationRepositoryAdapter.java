package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import com.arka.directory.domain.organizationcontext.repository.OrganizationRepository;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationId;
import com.arka.directory.infrastructure.adapter.out.persistence.mapper.DirectoryRowMapper;
import com.arka.directory.infrastructure.adapter.out.persistence.repository.ReactiveOrganizationRepository;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DomainOrganizationRepositoryAdapter implements OrganizationRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final ReactiveOrganizationRepository repository;
    private final DirectoryRowMapper rowMapper;

    public DomainOrganizationRepositoryAdapter(
            ReactiveOrganizationRepository repository,
            DirectoryRowMapper rowMapper) {
        this.repository = repository;
        this.rowMapper = rowMapper;
    }

    @Override
    public Organization save(Organization organization) {
        Organization saved = repository.save(rowMapper.toRow(organization)).map(rowMapper::toDomain).block(BLOCK_TIMEOUT);
        if (saved == null) {
            throw new IllegalStateException("Organization persistence returned empty result");
        }
        return saved;
    }

    @Override
    public Optional<Organization> findById(OrganizationId organizationId) {
        return repository.findById(organizationId.value()).map(rowMapper::toDomain).blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public Optional<Organization> findByCode(String organizationCode) {
        return repository.findByOrganizationCodeIgnoreCase(organizationCode).map(rowMapper::toDomain).blockOptional(BLOCK_TIMEOUT);
    }
}
