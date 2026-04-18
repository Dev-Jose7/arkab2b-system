package com.arka.reporting.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("report_artifacts")
public record ReportArtifactRow(
        @Id
        @Column("artifact_id") String artifactId,
        @Column("execution_id") String executionId,
        @Column("tenant_id") String tenantId,
        @Column("week_id") String weekId,
        @Column("report_type") String reportType,
        @Column("format") String format,
        @Column("location_ref") String locationRef,
        @Column("content_hash") String contentHash,
        @Column("size_bytes") Long sizeBytes,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}
