package com.arka.directory.application.port.in;

import com.arka.directory.application.query.ListOrganizationContactsQuery;
import com.arka.directory.application.result.OrganizationContactResult;
import reactor.core.publisher.Flux;

public interface ListOrganizationContactsQueryUseCase {

    Flux<OrganizationContactResult> handle(ListOrganizationContactsQuery query);
}
