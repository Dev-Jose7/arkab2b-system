package com.arka.reporting.infrastructure.adapter.in.web.mapper.response;

import com.arka.reporting.application.result.AnalyticFactResult;
import com.arka.reporting.application.result.FactSearchItemResult;
import com.arka.reporting.application.result.FactSearchResult;
import com.arka.reporting.application.result.OperationsKpiResult;
import com.arka.reporting.application.result.ReportArtifactResult;
import com.arka.reporting.application.result.ReplenishmentProjectionResult;
import com.arka.reporting.application.result.ReportingAuditEntryResult;
import com.arka.reporting.application.result.ReportingAuditResult;
import com.arka.reporting.application.result.ReportingMetricsResult;
import com.arka.reporting.application.result.SalesProjectionResult;
import com.arka.reporting.application.result.WeeklyExecutionResult;
import com.arka.reporting.infrastructure.adapter.in.web.response.AnalyticFactResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.FactSearchItemResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.FactSearchResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.OperationsKpiResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.ReportArtifactResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.ReplenishmentProjectionResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.ReportingAuditEntryResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.ReportingAuditResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.ReportingMetricsResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.SalesProjectionResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.WeeklyExecutionResponse;
import org.springframework.stereotype.Component;

@Component
public class ReportingResponseMapper {

    public AnalyticFactResponse toResponse(AnalyticFactResult result) {
        return new AnalyticFactResponse(
                result.factId(),
                result.organizationId(),
                result.sourceEventId(),
                result.eventType(),
                result.factType(),
                result.factStatus(),
                result.rawPayload(),
                result.normalizedPayload(),
                result.rejectionReason(),
                result.occurredAt(),
                result.createdAt(),
                result.updatedAt());
    }

    public FactSearchItemResponse toResponse(FactSearchItemResult result) {
        return new FactSearchItemResponse(
                result.factId(),
                result.sourceEventId(),
                result.eventType(),
                result.factType(),
                result.factStatus(),
                result.period(),
                result.occurredAt(),
                result.updatedAt());
    }

    public FactSearchResponse toResponse(FactSearchResult result) {
        return new FactSearchResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements());
    }

    public SalesProjectionResponse toResponse(SalesProjectionResult result) {
        return new SalesProjectionResponse(
                result.projectionId(),
                result.organizationId(),
                result.period(),
                result.totalSales(),
                result.paidAmount(),
                result.pendingAmount(),
                result.confirmedOrders(),
                result.averageTicket());
    }

    public ReplenishmentProjectionResponse toResponse(ReplenishmentProjectionResult result) {
        return new ReplenishmentProjectionResponse(
                result.projectionId(),
                result.organizationId(),
                result.period(),
                result.sku(),
                result.availableQty(),
                result.reorderPoint(),
                result.coverageDays(),
                result.riskLevel());
    }

    public OperationsKpiResponse toResponse(OperationsKpiResult result) {
        return new OperationsKpiResponse(
                result.projectionId(),
                result.organizationId(),
                result.period(),
                result.kpiName(),
                result.kpiValue());
    }

    public WeeklyExecutionResponse toResponse(WeeklyExecutionResult result) {
        return new WeeklyExecutionResponse(
                result.executionId(),
                result.organizationId(),
                result.weekId(),
                result.reportType(),
                result.status(),
                result.errorCode(),
                result.errorMessage(),
                result.completionArtifactRef(),
                result.version(),
                result.createdAt(),
                result.startedAt(),
                result.completedAt(),
                result.updatedAt());
    }

    public ReportArtifactResponse toResponse(ReportArtifactResult result) {
        return new ReportArtifactResponse(
                result.artifactId(),
                result.executionId(),
                result.organizationId(),
                result.weekId(),
                result.reportType(),
                result.format(),
                result.locationRef(),
                result.contentHash(),
                result.sizeBytes(),
                result.createdAt(),
                result.updatedAt());
    }

    public ReportingMetricsResponse toResponse(ReportingMetricsResult result) {
        return new ReportingMetricsResponse(
                result.weeklySalesTotal(),
                result.weeklyCollectionRate(),
                result.replenishmentHighRiskCount(),
                result.notificationEffectiveness(),
                result.consumerLag());
    }

    public ReportingAuditResponse toResponse(ReportingAuditResult result) {
        return new ReportingAuditResponse(
                result.entries().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements());
    }

    public ReportingAuditEntryResponse toResponse(ReportingAuditEntryResult result) {
        return new ReportingAuditEntryResponse(
                result.auditId(),
                result.organizationId(),
                result.actorId(),
                result.actionType(),
                result.targetType(),
                result.targetId(),
                result.outcome(),
                result.payload(),
                result.idempotencyKey(),
                result.createdAt());
    }
}
