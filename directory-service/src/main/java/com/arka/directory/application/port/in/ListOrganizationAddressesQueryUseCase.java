package com.arka.directory.application.port.in;

import com.arka.directory.application.query.ListOrganizationAddressesQuery;
import com.arka.directory.application.result.AddressResult;
import reactor.core.publisher.Flux;

public interface ListOrganizationAddressesQueryUseCase {

    Flux<AddressResult> handle(ListOrganizationAddressesQuery query);
}
