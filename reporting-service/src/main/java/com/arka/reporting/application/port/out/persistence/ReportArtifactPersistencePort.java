package com.arka.reporting.application.port.out.persistence;

import com.arka.reporting.domain.weeklyreportexecution.entity.ReportArtifact;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReportArtifactPersistencePort {

    Mono<ReportArtifact> create(ReportArtifact artifact);

    Mono<ReportArtifact> findById(String tenantId, String artifactId);

    Flux<ReportArtifact> findByExecutionId(String tenantId, String executionId);

    Flux<ReportArtifact> findByWeekAndType(String tenantId, String weekId, String reportType, int offset, int limit);

    Mono<Long> countByWeekAndType(String tenantId, String weekId, String reportType);
}
