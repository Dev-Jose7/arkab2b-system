package com.arka.reporting.application.port.in;

import com.arka.reporting.application.query.GetOperationsKpiQuery;
import com.arka.reporting.application.result.OperationsKpiResult;
import reactor.core.publisher.Flux;

public interface GetOperationsKpiQueryUseCase {

    Flux<OperationsKpiResult> handle(GetOperationsKpiQuery query);
}
