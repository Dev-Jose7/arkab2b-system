package com.arka.reporting.application.port.in;

import com.arka.reporting.application.query.GetReportingAuditQuery;
import com.arka.reporting.application.result.ReportingAuditResult;
import reactor.core.publisher.Mono;

public interface GetReportingAuditQueryUseCase {

    Mono<ReportingAuditResult> handle(GetReportingAuditQuery query);
}
