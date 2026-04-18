package com.arka.reporting.application.port.in;

import com.arka.reporting.application.query.ListReportArtifactsQuery;
import com.arka.reporting.application.result.ReportArtifactResult;
import reactor.core.publisher.Flux;

public interface ListReportArtifactsQueryUseCase {

    Flux<ReportArtifactResult> handle(ListReportArtifactsQuery query);
}
