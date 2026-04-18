package com.arka.directory.application.port.in;

import com.arka.directory.application.query.GetOrganizationQuery;
import com.arka.directory.application.result.OrganizationResult;
import reactor.core.publisher.Mono;

public interface GetOrganizationQueryUseCase {

    Mono<OrganizationResult> handle(GetOrganizationQuery query);
}
