package com.arka.directory.application.port.out.persistence;

import com.arka.directory.domain.organizationcontext.entity.Address;
import com.arka.directory.domain.organizationcontext.enumtype.AddressType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DirectoryAddressPersistencePort {

    Mono<Address> upsert(Address address);

    Mono<Address> findById(String organizationId, String addressId);

    Flux<Address> findByOrganizationId(String organizationId);

    Mono<Void> clearDefaultForType(String organizationId, AddressType addressType);

    Mono<Long> countActive();
}
