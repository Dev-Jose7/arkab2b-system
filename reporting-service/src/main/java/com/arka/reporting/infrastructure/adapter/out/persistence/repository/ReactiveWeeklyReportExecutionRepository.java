package com.arka.reporting.infrastructure.adapter.out.persistence.repository;

import com.arka.reporting.infrastructure.adapter.out.persistence.entity.WeeklyReportExecutionRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveWeeklyReportExecutionRepository extends ReactiveCrudRepository<WeeklyReportExecutionRow, String> {

    @Query("SELECT * FROM weekly_report_executions WHERE organization_id = :organizationId AND execution_id = :executionId")
    Mono<WeeklyReportExecutionRow> findByOrganizationAndId(String organizationId, String executionId);

    @Query("SELECT * FROM weekly_report_executions WHERE organization_id = :organizationId AND week_id = :weekId AND report_type = :reportType")
    Mono<WeeklyReportExecutionRow> findByOrganizationWeekAndType(String organizationId, String weekId, String reportType);

    @Query("""
            UPDATE weekly_report_executions
            SET status = :status,
                error_code = :errorCode,
                error_message = :errorMessage,
                completion_artifact_ref = :completionArtifactRef,
                version = :nextVersion,
                started_at = :startedAt,
                completed_at = :completedAt,
                updated_at = :updatedAt
            WHERE organization_id = :organizationId
              AND execution_id = :executionId
              AND version = :expectedVersion
            """)
    Mono<Integer> updateOptimistic(
            String organizationId,
            String executionId,
            String status,
            String errorCode,
            String errorMessage,
            String completionArtifactRef,
            long expectedVersion,
            long nextVersion,
            Instant startedAt,
            Instant completedAt,
            Instant updatedAt);

    @Query("""
            SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
            FROM weekly_report_executions
            WHERE organization_id = :organizationId
              AND report_type = 'FULL_REBUILD'
              AND status = 'RUNNING'
            """)
    Mono<Boolean> existsRunningRebuild(String organizationId);
}
