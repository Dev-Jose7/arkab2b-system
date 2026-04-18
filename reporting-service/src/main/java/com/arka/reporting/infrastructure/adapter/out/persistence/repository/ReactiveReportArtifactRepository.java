package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ReportArtifactRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveReportArtifactRepository extends ReactiveCrudRepository<ReportArtifactRow, String> {

    @Query("SELECT * FROM report_artifacts WHERE organization_id = :organizationId AND artifact_id = :artifactId")
    Mono<ReportArtifactRow> findByOrganizationAndId(String organizationId, String artifactId);

    @Query("""
            SELECT *
            FROM report_artifacts
            WHERE organization_id = :organizationId
              AND execution_id = :executionId
            ORDER BY created_at DESC
            """)
    Flux<ReportArtifactRow> findByExecutionId(String organizationId, String executionId);

    @Query("""
            SELECT *
            FROM report_artifacts
            WHERE organization_id = :organizationId
              AND week_id = :weekId
              AND report_type = :reportType
            ORDER BY created_at DESC
            OFFSET :offset
            LIMIT :limit
            """)
    Flux<ReportArtifactRow> findByWeekAndType(String organizationId, String weekId, String reportType, int offset, int limit);

    @Query("""
            SELECT COUNT(*)
            FROM report_artifacts
            WHERE organization_id = :organizationId
              AND week_id = :weekId
              AND report_type = :reportType
            """)
    Mono<Long> countByWeekAndType(String organizationId, String weekId, String reportType);

    @Query("""
            SELECT *
            FROM report_artifacts
            WHERE organization_id = :organizationId
              AND week_id = :weekId
              AND report_type = :reportType
              AND format = :format
            LIMIT 1
            """)
    Mono<ReportArtifactRow> findByUnique(String organizationId, String weekId, String reportType, String format);
}
