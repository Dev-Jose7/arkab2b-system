package com.arka.reporting.application.port.in;

import com.arka.reporting.application.query.GetReportingMetricsQuery;
import com.arka.reporting.application.result.ReportingMetricsResult;
import reactor.core.publisher.Mono;

public interface GetReportingMetricsQueryUseCase {

    Mono<ReportingMetricsResult> handle(GetReportingMetricsQuery query);
}
