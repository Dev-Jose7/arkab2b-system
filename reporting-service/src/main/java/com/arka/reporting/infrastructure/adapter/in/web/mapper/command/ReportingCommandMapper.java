package com.arka.reporting.infrastructure.adapter.in.web.mapper.command;

import com.arka.reporting.application.command.ApplyAnalyticFactCommand;
import com.arka.reporting.application.command.GenerateReportArtifactCommand;
import com.arka.reporting.application.command.GenerateWeeklyReplenishmentReportCommand;
import com.arka.reporting.application.command.GenerateWeeklySalesReportCommand;
import com.arka.reporting.application.command.RebuildProjectionCommand;
import com.arka.reporting.application.command.RegisterAnalyticFactCommand;
import com.arka.reporting.application.command.ReprocessReportingDlqCommand;
import com.arka.reporting.application.command.UpdateConsumerCheckpointCommand;
import com.arka.reporting.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.reporting.infrastructure.adapter.in.web.request.ApplyAnalyticFactRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.GenerateReportArtifactRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.GenerateWeeklyReportRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.RebuildProjectionRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.RegisterAnalyticFactRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.ReprocessReportingDlqRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.UpdateConsumerCheckpointRequest;
import org.springframework.stereotype.Component;

@Component
public class ReportingCommandMapper {

    public RegisterAnalyticFactCommand toCommand(RegisterAnalyticFactRequest request, IamSecurityPrincipal principal) {
        return new RegisterAnalyticFactCommand(
                principal.organizationId(),
                principal.actorId(),
                request.sourceEventId(),
                request.sourceEventType(),
                request.factType(),
                request.payloadJson(),
                request.occurredAt(),
                request.consumerName(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public ApplyAnalyticFactCommand toCommand(String factId, ApplyAnalyticFactRequest request, IamSecurityPrincipal principal) {
        return new ApplyAnalyticFactCommand(
                principal.organizationId(),
                principal.actorId(),
                factId,
                request == null ? null : normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public UpdateConsumerCheckpointCommand toCommand(UpdateConsumerCheckpointRequest request, IamSecurityPrincipal principal) {
        return new UpdateConsumerCheckpointCommand(
                principal.organizationId(),
                principal.actorId(),
                request.consumerName(),
                request.topic(),
                request.partition(),
                request.currentOffset(),
                request.latestOffset(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public RebuildProjectionCommand toCommand(RebuildProjectionRequest request, IamSecurityPrincipal principal) {
        return new RebuildProjectionCommand(
                principal.organizationId(),
                principal.actorId(),
                request.fullRebuild(),
                request.weekId(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public GenerateWeeklySalesReportCommand toWeeklySalesCommand(
            GenerateWeeklyReportRequest request,
            IamSecurityPrincipal principal) {
        return new GenerateWeeklySalesReportCommand(
                principal.organizationId(),
                principal.actorId(),
                request == null ? null : request.weekId(),
                request == null ? null : request.format(),
                request == null ? null : normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public GenerateWeeklyReplenishmentReportCommand toWeeklyReplenishmentCommand(
            GenerateWeeklyReportRequest request,
            IamSecurityPrincipal principal) {
        return new GenerateWeeklyReplenishmentReportCommand(
                principal.organizationId(),
                principal.actorId(),
                request == null ? null : request.weekId(),
                request == null ? null : request.format(),
                request == null ? null : normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public GenerateReportArtifactCommand toCommand(
            String executionId,
            GenerateReportArtifactRequest request,
            IamSecurityPrincipal principal) {
        return new GenerateReportArtifactCommand(
                principal.organizationId(),
                principal.actorId(),
                executionId,
                request == null ? null : request.weekId(),
                request == null ? null : request.reportType(),
                request == null ? null : request.format(),
                request == null ? null : request.payload(),
                request == null ? null : normalizeIdempotencyKey(request.idempotencyKey()));
    }

    public ReprocessReportingDlqCommand toCommand(ReprocessReportingDlqRequest request, IamSecurityPrincipal principal) {
        return new ReprocessReportingDlqCommand(
                principal.organizationId(),
                principal.actorId(),
                request.dlqEventId(),
                request.consumerName(),
                request.factId(),
                normalizeIdempotencyKey(request.idempotencyKey()));
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
