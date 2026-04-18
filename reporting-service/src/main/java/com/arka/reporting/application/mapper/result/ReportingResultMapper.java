package com.arka.reporting.application.mapper.result;

import com.arka.reporting.application.port.out.audit.ReportingAuditEntry;
import com.arka.reporting.application.port.out.persistence.FactSearchProjection;
import com.arka.reporting.application.port.out.persistence.ReportingMetricsProjection;
import com.arka.reporting.application.result.AnalyticFactResult;
import com.arka.reporting.application.result.FactSearchItemResult;
import com.arka.reporting.application.result.OperationsKpiResult;
import com.arka.reporting.application.result.ReportArtifactResult;
import com.arka.reporting.application.result.ReplenishmentProjectionResult;
import com.arka.reporting.application.result.ReportingAuditEntryResult;
import com.arka.reporting.application.result.ReportingMetricsResult;
import com.arka.reporting.application.result.SalesProjectionResult;
import com.arka.reporting.application.result.WeeklyExecutionResult;
import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.weeklyreportexecution.entity.OperationsKpiProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.ReportArtifact;
import com.arka.reporting.domain.weeklyreportexecution.entity.ReplenishmentProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.SalesProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ReportingResultMapper {

    public AnalyticFactResult toResult(AnalyticFact fact) {
        return new AnalyticFactResult(
                fact.factId().value(),
                fact.organizationId().value(),
                fact.sourceEventId().value(),
                fact.eventType(),
                fact.factType().name(),
                fact.factStatus().name(),
                fact.rawPayload(),
                fact.normalizedPayload(),
                fact.rejectionReason(),
                fact.occurredAt(),
                fact.createdAt(),
                fact.updatedAt());
    }

    public FactSearchItemResult toResult(FactSearchProjection projection) {
        return new FactSearchItemResult(
                projection.factId(),
                projection.sourceEventId(),
                projection.eventType(),
                projection.factType(),
                projection.factStatus(),
                projection.period(),
                projection.occurredAt(),
                projection.updatedAt());
    }

    public SalesProjectionResult toResult(SalesProjection projection) {
        return new SalesProjectionResult(
                projection.projectionId(),
                projection.organizationId(),
                projection.period(),
                projection.totalSales(),
                projection.paidAmount(),
                projection.pendingAmount(),
                projection.confirmedOrders(),
                projection.averageTicket());
    }

    public ReplenishmentProjectionResult toResult(ReplenishmentProjection projection) {
        return new ReplenishmentProjectionResult(
                projection.projectionId(),
                projection.organizationId(),
                projection.period(),
                projection.sku(),
                projection.availableQty(),
                projection.reorderPoint(),
                projection.coverageDays(),
                projection.riskLevel().name());
    }

    public OperationsKpiResult toResult(OperationsKpiProjection projection) {
        return new OperationsKpiResult(
                projection.projectionId(),
                projection.organizationId(),
                projection.period(),
                projection.kpiName(),
                projection.kpiValue());
    }

    public WeeklyExecutionResult toResult(WeeklyReportExecution execution) {
        return new WeeklyExecutionResult(
                execution.executionId().value(),
                execution.organizationId().value(),
                execution.weekId().value(),
                execution.reportType().name(),
                execution.status().name(),
                execution.errorCode(),
                execution.errorMessage(),
                execution.completionArtifactRef(),
                execution.version(),
                execution.createdAt(),
                execution.startedAt(),
                execution.completedAt(),
                execution.updatedAt());
    }

    public ReportArtifactResult toResult(ReportArtifact artifact) {
        return new ReportArtifactResult(
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

    public ReportingMetricsResult toResult(ReportingMetricsProjection projection) {
        if (projection == null) {
            return new ReportingMetricsResult(BigDecimal.ZERO, BigDecimal.ZERO, 0L, BigDecimal.ZERO, 0L);
        }
        return new ReportingMetricsResult(
                safe(projection.weeklySalesTotal()),
                safe(projection.weeklyCollectionRate()),
                projection.replenishmentHighRiskCount(),
                safe(projection.notificationEffectiveness()),
                projection.consumerLag());
    }

    public ReportingAuditEntryResult toResult(ReportingAuditEntry entry) {
        return new ReportingAuditEntryResult(
                entry.auditId(),
                entry.organizationId(),
                entry.actorId(),
                entry.actionType(),
                entry.targetType(),
                entry.targetId(),
                entry.outcome(),
                entry.payload(),
                entry.idempotencyKey(),
                entry.createdAt());
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
