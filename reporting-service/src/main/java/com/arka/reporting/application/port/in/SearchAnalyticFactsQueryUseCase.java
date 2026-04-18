package com.arka.reporting.application.port.in;

import com.arka.reporting.application.query.SearchAnalyticFactsQuery;
import com.arka.reporting.application.result.FactSearchResult;
import reactor.core.publisher.Mono;

public interface SearchAnalyticFactsQueryUseCase {

    Mono<FactSearchResult> handle(SearchAnalyticFactsQuery query);
}
