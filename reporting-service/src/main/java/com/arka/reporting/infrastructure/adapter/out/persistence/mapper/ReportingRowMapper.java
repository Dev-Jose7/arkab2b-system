package com.arka.reporting.infrastructure.adapter.out.persistence.mapper;

import com.arka.reporting.application.port.out.audit.ReportingAuditEntry;
import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.analyticfact.enumtype.AnalyticFactStatus;
import com.arka.reporting.domain.analyticfact.enumtype.AnalyticFactType;
import com.arka.reporting.domain.analyticfact.valueobject.FactId;
import com.arka.reporting.domain.analyticfact.valueobject.SourceEventId;
import com.arka.reporting.domain.weeklyreportexecution.entity.ConsumerCheckpoint;
import com.arka.reporting.domain.weeklyreportexecution.entity.OperationsKpiProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.ReportArtifact;
import com.arka.reporting.domain.weeklyreportexecution.entity.ReplenishmentProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.SalesProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.ReportType;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.RiskLevel;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.WeeklyReportExecutionStatus;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.ExecutionId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeekId;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.AnalyticFactRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ConsumerCheckpointRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.OperationsKpiProjectionRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ReportArtifactRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ReplenishmentProjectionRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.ReportingAuditRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.SalesProjectionRow;
import com.arka.reporting.infrastructure.adapter.out.persistence.entity.WeeklyReportExecutionRow;
import org.springframework.stereotype.Component;

@Component
public class ReportingRowMapper {

    public AnalyticFactRow toRow(AnalyticFact fact, String period) {
        return new AnalyticFactRow(
                fact.factId().value(),
                fact.organizationId().value(),
                fact.sourceEventId().value(),
                fact.eventType(),
                fact.factType().name(),
                fact.rawPayload(),
                emptyAsNull(fact.normalizedPayload()),
                fact.factStatus().name(),
                emptyAsNull(fact.rejectionReason()),
                period,
                fact.occurredAt(),
                fact.createdAt(),
                fact.updatedAt());
    }

    public AnalyticFact toDomain(AnalyticFactRow row) {
        return AnalyticFact.rehydrate(
                FactId.of(row.factId()),
                com.arka.reporting.domain.analyticfact.valueobject.OrganizationId.of(row.organizationId()),
                SourceEventId.of(row.sourceEventId()),
                row.eventType(),
                AnalyticFactType.valueOf(row.factType()),
                row.rawPayload(),
                row.normalizedPayload(),
                AnalyticFactStatus.valueOf(row.factStatus()),
                row.rejectionReason(),
                row.occurredAt(),
                row.createdAt(),
                row.updatedAt());
    }

    public SalesProjection toDomain(SalesProjectionRow row) {
        return new SalesProjection(
                row.projectionId(),
                row.organizationId(),
                row.period(),
                row.totalSales(),
                row.paidAmount(),
                row.pendingAmount(),
                row.confirmedOrders() == null ? 0L : row.confirmedOrders(),
                row.averageTicket(),
                row.version() == null ? 0L : row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    public ReplenishmentProjection toDomain(ReplenishmentProjectionRow row) {
        return new ReplenishmentProjection(
                row.projectionId(),
                row.organizationId(),
                row.period(),
                row.sku(),
                row.availableQty(),
                row.reorderPoint(),
                row.coverageDays(),
                RiskLevel.valueOf(row.riskLevel()),
                row.version() == null ? 0L : row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    public OperationsKpiProjection toDomain(OperationsKpiProjectionRow row) {
        return new OperationsKpiProjection(
                row.projectionId(),
                row.organizationId(),
                row.period(),
                row.kpiName(),
                row.kpiValue(),
                row.version() == null ? 0L : row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    public WeeklyReportExecutionRow toRow(WeeklyReportExecution execution) {
        return new WeeklyReportExecutionRow(
                execution.executionId().value(),
                execution.organizationId().value(),
                execution.weekId().value(),
                execution.reportType().name(),
                execution.status().name(),
                emptyAsNull(execution.errorCode()),
                emptyAsNull(execution.errorMessage()),
                emptyAsNull(execution.completionArtifactRef()),
                execution.version(),
                execution.createdAt(),
                execution.startedAt(),
                execution.completedAt(),
                execution.updatedAt());
    }

    public WeeklyReportExecution toDomain(WeeklyReportExecutionRow row) {
        return WeeklyReportExecution.rehydrate(
                ExecutionId.of(row.executionId()),
                com.arka.reporting.domain.weeklyreportexecution.valueobject.OrganizationId.of(row.organizationId()),
                WeekId.of(row.weekId()),
                ReportType.valueOf(row.reportType()),
                WeeklyReportExecutionStatus.valueOf(row.status()),
                row.errorCode(),
                row.errorMessage(),
                row.completionArtifactRef(),
                row.version() == null ? 0L : row.version(),
                row.createdAt(),
                row.startedAt(),
                row.completedAt(),
                row.updatedAt());
    }

    public ReportArtifactRow toRow(ReportArtifact artifact) {
        return new ReportArtifactRow(
                artifact.artifactId(),
                artifact.executionId(),
                artifact.organizationId(),
                artifact.weekId(),
                artifact.reportType(),
                artifact.format(),
                artifact.locationRef(),
                artifact.contentHash(),
                artifact.sizeBytes(),
                artifact.createdAt(),
                artifact.updatedAt());
    }

    public ReportArtifact toDomain(ReportArtifactRow row) {
        return new ReportArtifact(
                row.artifactId(),
                row.executionId(),
                row.organizationId(),
                row.weekId(),
                row.reportType(),
                row.format(),
                row.locationRef(),
                row.contentHash(),
                row.sizeBytes() == null ? 0L : row.sizeBytes(),
                row.createdAt(),
                row.updatedAt());
    }

    public ConsumerCheckpoint toDomain(ConsumerCheckpointRow row) {
        return new ConsumerCheckpoint(
                row.checkpointId(),
                row.organizationId(),
                row.consumerName(),
                row.topic(),
                row.partition() == null ? 0 : row.partition(),
                row.currentOffset() == null ? 0L : row.currentOffset(),
                row.latestOffset() == null ? 0L : row.latestOffset(),
                row.lag() == null ? 0L : row.lag(),
                row.updatedAt());
    }

    public ReportingAuditRow toRow(ReportingAuditEntry entry) {
        return new ReportingAuditRow(
                entry.auditId(),
                entry.organizationId(),
                entry.actorId(),
                entry.actionType(),
                entry.targetType(),
                entry.targetId(),
                entry.outcome(),
                entry.payload(),
                entry.idempotencyKey(),
                entry.payloadHash(),
                entry.createdAt());
    }

    public ReportingAuditEntry toDomain(ReportingAuditRow row) {
        return new ReportingAuditEntry(
                row.auditId(),
                row.organizationId(),
                row.actorId(),
                row.actionType(),
                row.targetType(),
                row.targetId(),
                row.outcome(),
                row.payload(),
                row.idempotencyKey(),
                row.payloadHash(),
                row.createdAt());
    }

    private String emptyAsNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
