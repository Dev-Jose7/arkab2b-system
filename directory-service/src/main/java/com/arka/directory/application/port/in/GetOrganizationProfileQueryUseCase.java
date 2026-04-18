package com.arka.directory.application.port.in;

import com.arka.directory.application.query.GetOrganizationProfileQuery;
import com.arka.directory.application.result.OrganizationProfileResult;
import reactor.core.publisher.Mono;

public interface GetOrganizationProfileQueryUseCase {

    Mono<OrganizationProfileResult> handle(GetOrganizationProfileQuery query);
}
