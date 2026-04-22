package com.arka.reporting.infrastructure.adapter.in.web.controller;

import com.arka.reporting.application.port.in.ApplyAnalyticFactCommandUseCase;
import com.arka.reporting.application.port.in.GenerateReportArtifactCommandUseCase;
import com.arka.reporting.application.port.in.GenerateWeeklyReplenishmentReportCommandUseCase;
import com.arka.reporting.application.port.in.GenerateWeeklySalesReportCommandUseCase;
import com.arka.reporting.application.port.in.GetAnalyticFactByIdQueryUseCase;
import com.arka.reporting.application.port.in.GetOperationsKpiQueryUseCase;
import com.arka.reporting.application.port.in.GetReportingAuditQueryUseCase;
import com.arka.reporting.application.port.in.GetReportingMetricsQueryUseCase;
import com.arka.reporting.application.port.in.GetWeeklyExecutionQueryUseCase;
import com.arka.reporting.application.port.in.GetWeeklyReplenishmentProjectionQueryUseCase;
import com.arka.reporting.application.port.in.GetWeeklySalesProjectionQueryUseCase;
import com.arka.reporting.application.port.in.ListReportArtifactsQueryUseCase;
import com.arka.reporting.application.port.in.RebuildProjectionCommandUseCase;
import com.arka.reporting.application.port.in.RegisterAnalyticFactCommandUseCase;
import com.arka.reporting.application.port.in.ReprocessReportingDlqCommandUseCase;
import com.arka.reporting.application.port.in.SearchAnalyticFactsQueryUseCase;
import com.arka.reporting.application.port.in.UpdateConsumerCheckpointCommandUseCase;
import com.arka.reporting.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.reporting.infrastructure.adapter.in.web.mapper.command.ReportingCommandMapper;
import com.arka.reporting.infrastructure.adapter.in.web.mapper.query.ReportingQueryMapper;
import com.arka.reporting.infrastructure.adapter.in.web.mapper.response.ReportingResponseMapper;
import com.arka.reporting.infrastructure.adapter.in.web.request.ApplyAnalyticFactRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.GenerateReportArtifactRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.GenerateWeeklyReportRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.RebuildProjectionRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.RegisterAnalyticFactRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.ReprocessReportingDlqRequest;
import com.arka.reporting.infrastructure.adapter.in.web.request.UpdateConsumerCheckpointRequest;
import com.arka.reporting.infrastructure.adapter.in.web.response.AnalyticFactResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.FactSearchResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.GeneratedBusinessReportResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.OperationsKpiResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.ReportArtifactResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.ReplenishmentProjectionResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.ReportingAuditResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.ReportingMetricsResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.SalesProjectionResponse;
import com.arka.reporting.infrastructure.adapter.in.web.response.WeeklyExecutionResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/reporting")
public class ReportingController {

    private final ReportingCommandMapper commandMapper;
    private final ReportingQueryMapper queryMapper;
    private final ReportingResponseMapper responseMapper;
    private final RegisterAnalyticFactCommandUseCase registerAnalyticFactCommandUseCase;
    private final ApplyAnalyticFactCommandUseCase applyAnalyticFactCommandUseCase;
    private final UpdateConsumerCheckpointCommandUseCase updateConsumerCheckpointCommandUseCase;
    private final RebuildProjectionCommandUseCase rebuildProjectionCommandUseCase;
    private final GenerateWeeklySalesReportCommandUseCase generateWeeklySalesReportCommandUseCase;
    private final GenerateWeeklyReplenishmentReportCommandUseCase generateWeeklyReplenishmentReportCommandUseCase;
    private final GenerateReportArtifactCommandUseCase generateReportArtifactCommandUseCase;
    private final ReprocessReportingDlqCommandUseCase reprocessReportingDlqCommandUseCase;
    private final GetAnalyticFactByIdQueryUseCase getAnalyticFactByIdQueryUseCase;
    private final SearchAnalyticFactsQueryUseCase searchAnalyticFactsQueryUseCase;
    private final GetWeeklySalesProjectionQueryUseCase getWeeklySalesProjectionQueryUseCase;
    private final GetWeeklyReplenishmentProjectionQueryUseCase getWeeklyReplenishmentProjectionQueryUseCase;
    private final GetOperationsKpiQueryUseCase getOperationsKpiQueryUseCase;
    private final GetWeeklyExecutionQueryUseCase getWeeklyExecutionQueryUseCase;
    private final ListReportArtifactsQueryUseCase listReportArtifactsQueryUseCase;
    private final GetReportingMetricsQueryUseCase getReportingMetricsQueryUseCase;
    private final GetReportingAuditQueryUseCase getReportingAuditQueryUseCase;

    public ReportingController(
            ReportingCommandMapper commandMapper,
            ReportingQueryMapper queryMapper,
            ReportingResponseMapper responseMapper,
            RegisterAnalyticFactCommandUseCase registerAnalyticFactCommandUseCase,
            ApplyAnalyticFactCommandUseCase applyAnalyticFactCommandUseCase,
            UpdateConsumerCheckpointCommandUseCase updateConsumerCheckpointCommandUseCase,
            RebuildProjectionCommandUseCase rebuildProjectionCommandUseCase,
            GenerateWeeklySalesReportCommandUseCase generateWeeklySalesReportCommandUseCase,
            GenerateWeeklyReplenishmentReportCommandUseCase generateWeeklyReplenishmentReportCommandUseCase,
            GenerateReportArtifactCommandUseCase generateReportArtifactCommandUseCase,
            ReprocessReportingDlqCommandUseCase reprocessReportingDlqCommandUseCase,
            GetAnalyticFactByIdQueryUseCase getAnalyticFactByIdQueryUseCase,
            SearchAnalyticFactsQueryUseCase searchAnalyticFactsQueryUseCase,
            GetWeeklySalesProjectionQueryUseCase getWeeklySalesProjectionQueryUseCase,
            GetWeeklyReplenishmentProjectionQueryUseCase getWeeklyReplenishmentProjectionQueryUseCase,
            GetOperationsKpiQueryUseCase getOperationsKpiQueryUseCase,
            GetWeeklyExecutionQueryUseCase getWeeklyExecutionQueryUseCase,
            ListReportArtifactsQueryUseCase listReportArtifactsQueryUseCase,
            GetReportingMetricsQueryUseCase getReportingMetricsQueryUseCase,
            GetReportingAuditQueryUseCase getReportingAuditQueryUseCase) {
        this.commandMapper = commandMapper;
        this.queryMapper = queryMapper;
        this.responseMapper = responseMapper;
        this.registerAnalyticFactCommandUseCase = registerAnalyticFactCommandUseCase;
        this.applyAnalyticFactCommandUseCase = applyAnalyticFactCommandUseCase;
        this.updateConsumerCheckpointCommandUseCase = updateConsumerCheckpointCommandUseCase;
        this.rebuildProjectionCommandUseCase = rebuildProjectionCommandUseCase;
        this.generateWeeklySalesReportCommandUseCase = generateWeeklySalesReportCommandUseCase;
        this.generateWeeklyReplenishmentReportCommandUseCase = generateWeeklyReplenishmentReportCommandUseCase;
        this.generateReportArtifactCommandUseCase = generateReportArtifactCommandUseCase;
        this.reprocessReportingDlqCommandUseCase = reprocessReportingDlqCommandUseCase;
        this.getAnalyticFactByIdQueryUseCase = getAnalyticFactByIdQueryUseCase;
        this.searchAnalyticFactsQueryUseCase = searchAnalyticFactsQueryUseCase;
        this.getWeeklySalesProjectionQueryUseCase = getWeeklySalesProjectionQueryUseCase;
        this.getWeeklyReplenishmentProjectionQueryUseCase = getWeeklyReplenishmentProjectionQueryUseCase;
        this.getOperationsKpiQueryUseCase = getOperationsKpiQueryUseCase;
        this.getWeeklyExecutionQueryUseCase = getWeeklyExecutionQueryUseCase;
        this.listReportArtifactsQueryUseCase = listReportArtifactsQueryUseCase;
        this.getReportingMetricsQueryUseCase = getReportingMetricsQueryUseCase;
        this.getReportingAuditQueryUseCase = getReportingAuditQueryUseCase;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/facts")
    public Mono<AnalyticFactResponse> registerFact(
            @Valid @RequestBody RegisterAnalyticFactRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return registerAnalyticFactCommandUseCase
                .handle(commandMapper.toCommand(request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/facts/{factId}/apply")
    public Mono<AnalyticFactResponse> applyFact(
            @PathVariable String factId,
            @RequestBody(required = false) ApplyAnalyticFactRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return applyAnalyticFactCommandUseCase
                .handle(commandMapper.toCommand(factId, request, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/facts/{factId}")
    public Mono<AnalyticFactResponse> getFactById(
            @PathVariable String factId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getAnalyticFactByIdQueryUseCase
                .handle(queryMapper.toFactById(factId, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/facts")
    public Mono<FactSearchResponse> searchFacts(
            @RequestParam(name = "eventType", required = false) String eventType,
            @RequestParam(name = "factType", required = false) String factType,
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return searchAnalyticFactsQueryUseCase
                .handle(queryMapper.toFactSearch(eventType, factType, period, status, page, size, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/checkpoints")
    public Mono<Void> updateCheckpoint(
            @Valid @RequestBody UpdateConsumerCheckpointRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return updateConsumerCheckpointCommandUseCase.handle(commandMapper.toCommand(request, principal));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/rebuild")
    public Mono<WeeklyExecutionResponse> rebuild(
            @RequestBody(required = false) RebuildProjectionRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        RebuildProjectionRequest safeRequest = request == null
                ? new RebuildProjectionRequest(true, null, null)
                : request;
        return rebuildProjectionCommandUseCase
                .handle(commandMapper.toCommand(safeRequest, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/projections/sales")
    public Mono<SalesProjectionResponse> getSalesProjection(
            @RequestParam("period") String period,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getWeeklySalesProjectionQueryUseCase
                .handle(queryMapper.toSalesProjection(period, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/projections/replenishment")
    public Flux<ReplenishmentProjectionResponse> getReplenishmentProjection(
            @RequestParam("period") String period,
            @RequestParam(name = "sku", required = false) String sku,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getWeeklyReplenishmentProjectionQueryUseCase
                .handle(queryMapper.toReplenishmentProjection(period, sku, page, size, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/projections/kpis")
    public Flux<OperationsKpiResponse> getKpis(
            @RequestParam("period") String period,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getOperationsKpiQueryUseCase
                .handle(queryMapper.toKpiQuery(period, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/weekly-executions/sales")
    public Mono<WeeklyExecutionResponse> generateWeeklySales(
            @RequestBody(required = false) GenerateWeeklyReportRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return generateWeeklySalesReportCommandUseCase
                .handle(commandMapper.toWeeklySalesCommand(request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/reports/sales/weekly")
    public Mono<GeneratedBusinessReportResponse> generateWeeklySalesBusinessReport(
            @RequestBody(required = false) GenerateWeeklyReportRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        GenerateWeeklyReportRequest safeRequest = request == null
                ? new GenerateWeeklyReportRequest(null, null, null)
                : request;
        return generateWeeklySalesReportCommandUseCase
                .handle(commandMapper.toWeeklySalesCommand(safeRequest, principal))
                .flatMap(execution -> buildBusinessReportResponse(
                        "SALES",
                        "Reporte semanal de ventas generado.",
                        execution,
                        safeRequest,
                        principal));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/weekly-executions/replenishment")
    public Mono<WeeklyExecutionResponse> generateWeeklyReplenishment(
            @RequestBody(required = false) GenerateWeeklyReportRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return generateWeeklyReplenishmentReportCommandUseCase
                .handle(commandMapper.toWeeklyReplenishmentCommand(request, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/reports/replenishment/weekly")
    public Mono<GeneratedBusinessReportResponse> generateWeeklyReplenishmentBusinessReport(
            @RequestBody(required = false) GenerateWeeklyReportRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        GenerateWeeklyReportRequest safeRequest = request == null
                ? new GenerateWeeklyReportRequest(null, null, null)
                : request;
        return generateWeeklyReplenishmentReportCommandUseCase
                .handle(commandMapper.toWeeklyReplenishmentCommand(safeRequest, principal))
                .flatMap(execution -> buildBusinessReportResponse(
                        "REPLENISHMENT",
                        "Reporte semanal de abastecimiento generado.",
                        execution,
                        safeRequest,
                        principal));
    }

    @GetMapping("/weekly-executions/{executionId}")
    public Mono<WeeklyExecutionResponse> getExecutionById(
            @PathVariable String executionId,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getWeeklyExecutionQueryUseCase
                .handle(queryMapper.toExecutionById(executionId, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/weekly-executions")
    public Mono<WeeklyExecutionResponse> getExecutionByWeekAndType(
            @RequestParam("weekId") String weekId,
            @RequestParam("reportType") String reportType,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getWeeklyExecutionQueryUseCase
                .handle(queryMapper.toExecutionByWeekAndType(weekId, reportType, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/weekly-executions/{executionId}/artifacts")
    public Mono<ReportArtifactResponse> generateArtifact(
            @PathVariable String executionId,
            @RequestBody(required = false) GenerateReportArtifactRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return generateReportArtifactCommandUseCase
                .handle(commandMapper.toCommand(executionId, request, principal))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/weekly-executions/{executionId}/artifacts")
    public Flux<ReportArtifactResponse> listArtifactsByExecution(
            @PathVariable String executionId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getWeeklyExecutionQueryUseCase
                .handle(queryMapper.toExecutionById(executionId, principal))
                .flatMapMany(execution -> listReportArtifactsQueryUseCase
                        .handle(queryMapper.toArtifacts(execution.weekId(), execution.reportType(), page, size, principal)))
                .map(responseMapper::toResponse);
    }

    @GetMapping("/artifacts")
    public Flux<ReportArtifactResponse> listArtifactsByWeek(
            @RequestParam("weekId") String weekId,
            @RequestParam("reportType") String reportType,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return listReportArtifactsQueryUseCase
                .handle(queryMapper.toArtifacts(weekId, reportType, page, size, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @PostMapping("/reprocess-dlq")
    public Mono<Void> reprocessDlq(
            @Valid @RequestBody ReprocessReportingDlqRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return reprocessReportingDlqCommandUseCase.handle(commandMapper.toCommand(request, principal));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @GetMapping("/metrics")
    public Mono<ReportingMetricsResponse> metrics(
            @RequestParam(name = "period", required = false) String period,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getReportingMetricsQueryUseCase
                .handle(queryMapper.toMetricsQuery(period, principal))
                .map(responseMapper::toResponse);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_REPORTING_ADMIN','ROLE_INTERNAL_ACTOR','ROLE_ARKA_ADMIN')")
    @GetMapping("/audits")
    public Mono<ReportingAuditResponse> audits(
            @RequestParam(name = "targetType", required = false) String targetType,
            @RequestParam(name = "targetId", required = false) String targetId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return getReportingAuditQueryUseCase
                .handle(queryMapper.toAuditQuery(targetType, targetId, page, size, principal))
                .map(responseMapper::toResponse);
    }

    private Mono<GeneratedBusinessReportResponse> buildBusinessReportResponse(
            String reportType,
            String message,
            com.arka.reporting.application.result.WeeklyExecutionResult execution,
            GenerateWeeklyReportRequest request,
            IamSecurityPrincipal principal) {
        WeeklyExecutionResponse executionResponse = responseMapper.toResponse(execution);
        if (request == null || request.format() == null || request.format().isBlank()) {
            return Mono.just(new GeneratedBusinessReportResponse(
                    message,
                    reportType,
                    executionResponse,
                    null));
        }
        GenerateReportArtifactRequest artifactRequest = new GenerateReportArtifactRequest(
                execution.weekId(),
                reportType,
                request.format(),
                null,
                deriveArtifactIdempotencyKey(request.idempotencyKey(), reportType));
        return generateReportArtifactCommandUseCase
                .handle(commandMapper.toCommand(execution.executionId(), artifactRequest, principal))
                .map(responseMapper::toResponse)
                .map(artifact -> new GeneratedBusinessReportResponse(
                        message + " Artefacto exportable creado.",
                        reportType,
                        executionResponse,
                        artifact));
    }

    private String deriveArtifactIdempotencyKey(String idempotencyKey, String reportType) {
        String normalizedType = reportType == null ? "report" : reportType.toLowerCase();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return normalizedType + "-artifact-" + UUID.randomUUID();
        }
        return idempotencyKey.trim() + "-artifact";
    }
}
