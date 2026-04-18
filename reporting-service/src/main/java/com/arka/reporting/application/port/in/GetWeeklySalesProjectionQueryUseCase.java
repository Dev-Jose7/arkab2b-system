package com.arka.reporting.application.port.in;

import com.arka.reporting.application.query.GetWeeklySalesProjectionQuery;
import com.arka.reporting.application.result.SalesProjectionResult;
import reactor.core.publisher.Mono;

public interface GetWeeklySalesProjectionQueryUseCase {

    Mono<SalesProjectionResult> handle(GetWeeklySalesProjectionQuery query);
}
