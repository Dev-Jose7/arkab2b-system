package com.arka.reporting.application.port.in;

import com.arka.reporting.application.query.GetAnalyticFactByIdQuery;
import com.arka.reporting.application.result.AnalyticFactResult;
import reactor.core.publisher.Mono;

public interface GetAnalyticFactByIdQueryUseCase {

    Mono<AnalyticFactResult> handle(GetAnalyticFactByIdQuery query);
}
