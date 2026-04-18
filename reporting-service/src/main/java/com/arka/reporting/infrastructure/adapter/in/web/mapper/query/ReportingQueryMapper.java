package com.arka.reporting.infrastructure.adapter.in.web.mapper.query;

import com.arka.reporting.application.query.GetAnalyticFactByIdQuery;
import com.arka.reporting.application.query.GetOperationsKpiQuery;
import com.arka.reporting.application.query.GetReportingAuditQuery;
import com.arka.reporting.application.query.GetReportingMetricsQuery;
import com.arka.reporting.application.query.GetWeeklyExecutionQuery;
import com.arka.reporting.application.query.GetWeeklyReplenishmentProjectionQuery;
import com.arka.reporting.application.query.GetWeeklySalesProjectionQuery;
import com.arka.reporting.application.query.ListReportArtifactsQuery;
import com.arka.reporting.application.query.SearchAnalyticFactsQuery;
import com.arka.reporting.infrastructure.adapter.in.security.IamSecurityPrincipal;
import org.springframework.stereotype.Component;

@Component
public class ReportingQueryMapper {

    public GetAnalyticFactByIdQuery toFactById(String factId, IamSecurityPrincipal principal) {
        return new GetAnalyticFactByIdQuery(principal.organizationId(), factId);
    }

    public SearchAnalyticFactsQuery toFactSearch(
            String eventType,
            String factType,
            String period,
            String status,
            Integer page,
            Integer size,
            IamSecurityPrincipal principal) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        return new SearchAnalyticFactsQuery(
                principal.organizationId(),
                eventType,
                factType,
                period,
                status,
                safePage,
                safeSize);
    }

    public GetWeeklySalesProjectionQuery toSalesProjection(String period, IamSecurityPrincipal principal) {
        return new GetWeeklySalesProjectionQuery(principal.organizationId(), period);
    }

    public GetWeeklyReplenishmentProjectionQuery toReplenishmentProjection(
            String period,
            String sku,
            Integer page,
            Integer size,
            IamSecurityPrincipal principal) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        return new GetWeeklyReplenishmentProjectionQuery(principal.organizationId(), period, sku, safePage, safeSize);
    }

    public GetOperationsKpiQuery toKpiQuery(String period, IamSecurityPrincipal principal) {
        return new GetOperationsKpiQuery(principal.organizationId(), period);
    }

    public GetWeeklyExecutionQuery toExecutionById(String executionId, IamSecurityPrincipal principal) {
        return new GetWeeklyExecutionQuery(principal.organizationId(), executionId, null, null);
    }

    public GetWeeklyExecutionQuery toExecutionByWeekAndType(String weekId, String reportType, IamSecurityPrincipal principal) {
        return new GetWeeklyExecutionQuery(principal.organizationId(), null, weekId, reportType);
    }

    public ListReportArtifactsQuery toArtifacts(
            String weekId,
            String reportType,
            Integer page,
            Integer size,
            IamSecurityPrincipal principal) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        return new ListReportArtifactsQuery(principal.organizationId(), weekId, reportType, safePage, safeSize);
    }

    public GetReportingMetricsQuery toMetricsQuery(String period, IamSecurityPrincipal principal) {
        return new GetReportingMetricsQuery(principal.organizationId(), period);
    }

    public GetReportingAuditQuery toAuditQuery(
            String targetType,
            String targetId,
            Integer page,
            Integer size,
            IamSecurityPrincipal principal) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        return new GetReportingAuditQuery(principal.organizationId(), targetType, targetId, safePage, safeSize);
    }
}
