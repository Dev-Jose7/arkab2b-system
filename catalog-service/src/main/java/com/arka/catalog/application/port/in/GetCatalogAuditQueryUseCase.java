package com.arka.catalog.application.port.in;

import com.arka.catalog.application.query.GetCatalogAuditQuery;
import com.arka.catalog.application.result.CatalogAuditResult;
import reactor.core.publisher.Mono;

public interface GetCatalogAuditQueryUseCase {

    Mono<CatalogAuditResult> handle(GetCatalogAuditQuery query);
}
