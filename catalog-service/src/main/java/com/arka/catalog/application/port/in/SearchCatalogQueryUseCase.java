package com.arka.catalog.application.port.in;

import com.arka.catalog.application.query.SearchCatalogQuery;
import com.arka.catalog.application.result.CatalogSearchResult;
import reactor.core.publisher.Mono;

public interface SearchCatalogQueryUseCase {

    Mono<CatalogSearchResult> handle(SearchCatalogQuery query);
}
