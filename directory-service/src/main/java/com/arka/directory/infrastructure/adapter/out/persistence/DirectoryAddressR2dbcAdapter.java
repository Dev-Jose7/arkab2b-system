package com.arka.directory.infrastructure.adapter.out.persistence;

import com.arka.directory.application.port.out.persistence.DirectoryAddressPersistencePort;
import com.arka.directory.domain.organizationcontext.entity.Address;
import com.arka.directory.domain.organizationcontext.enumtype.AddressType;
import com.arka.directory.infrastructure.adapter.out.persistence.mapper.DirectoryRowMapper;
import com.arka.directory.infrastructure.adapter.out.persistence.repository.ReactiveAddressRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DirectoryAddressR2dbcAdapter implements DirectoryAddressPersistencePort {

    private final ReactiveAddressRepository repository;
    private final DirectoryRowMapper rowMapper;
    private final R2dbcEntityTemplate entityTemplate;

    public DirectoryAddressR2dbcAdapter(
            ReactiveAddressRepository repository,
            DirectoryRowMapper rowMapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.rowMapper = rowMapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Address> upsert(Address address) {
        var row = rowMapper.toRow(address);
        return repository.existsById(row.addressId())
                .flatMap(exists -> exists ? repository.save(row) : entityTemplate.insert(row))
                .map(rowMapper::toDomain);
    }

    @Override
    public Mono<Address> findById(String organizationId, String addressId) {
        return repository.findByOrganizationIdAndAddressId(organizationId, addressId).map(rowMapper::toDomain);
    }

    @Override
    public Flux<Address> findByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId).map(rowMapper::toDomain);
    }

    @Override
    public Mono<Void> clearDefaultForType(String organizationId, AddressType addressType) {
        return repository.clearDefaultForType(organizationId, addressType.name()).then();
    }

    @Override
    public Mono<Long> countActive() {
        return repository.countActive();
    }
}
