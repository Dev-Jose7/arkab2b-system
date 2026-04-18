package com.arka.reporting.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("weekly_report_executions")
public record WeeklyReportExecutionRow(
        @Id
        @Column("execution_id") String executionId,
        @Column("organization_id") String organizationId,
        @Column("week_id") String weekId,
        @Column("report_type") String reportType,
        @Column("status") String status,
        @Column("error_code") String errorCode,
        @Column("error_message") String errorMessage,
        @Column("completion_artifact_ref") String completionArtifactRef,
        @Column("version") Long version,
        @Column("created_at") Instant createdAt,
        @Column("started_at") Instant startedAt,
        @Column("completed_at") Instant completedAt,
        @Column("updated_at") Instant updatedAt) {
}
