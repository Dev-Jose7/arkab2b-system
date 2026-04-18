package com.arka.reporting.application.service;

import com.arka.reporting.application.command.ApplyAnalyticFactCommand;
import com.arka.reporting.application.command.GenerateReportArtifactCommand;
import com.arka.reporting.application.command.GenerateWeeklyReplenishmentReportCommand;
import com.arka.reporting.application.command.GenerateWeeklySalesReportCommand;
import com.arka.reporting.application.command.RebuildProjectionCommand;
import com.arka.reporting.application.command.RegisterAnalyticFactCommand;
import com.arka.reporting.application.command.ReprocessReportingDlqCommand;
import com.arka.reporting.application.command.UpdateConsumerCheckpointCommand;
import com.arka.reporting.application.exception.ActorNotLegitimateException;
import com.arka.reporting.application.exception.ApplicationException;
import com.arka.reporting.application.exception.IdempotencyConflictException;
import com.arka.reporting.application.exception.ReportingResourceNotFoundException;
import com.arka.reporting.application.exception.RegionalPolicyUnavailableException;
import com.arka.reporting.application.mapper.command.IdempotencySupport;
import com.arka.reporting.application.mapper.result.ReportingResultMapper;
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
import com.arka.reporting.application.port.out.audit.ReportingAuditEntry;
import com.arka.reporting.application.port.out.audit.ReportingAuditPort;
import com.arka.reporting.application.port.out.cache.ReportingSearchCachePort;
import com.arka.reporting.application.port.out.directory.RegionalPolicyPort;
import com.arka.reporting.application.port.out.event.DomainEventTopicPort;
import com.arka.reporting.application.port.out.external.ActorLegitimacyPort;
import com.arka.reporting.application.port.out.external.ArtifactStoragePort;
import com.arka.reporting.application.port.out.external.ClockPort;
import com.arka.reporting.application.port.out.external.StoredArtifact;
import com.arka.reporting.application.port.out.persistence.AnalyticFactPersistencePort;
import com.arka.reporting.application.port.out.persistence.ConsumerCheckpointPersistencePort;
import com.arka.reporting.application.port.out.persistence.FactSearchFilter;
import com.arka.reporting.application.port.out.persistence.OutboxPersistencePort;
import com.arka.reporting.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.reporting.application.port.out.persistence.ProjectionPersistencePort;
import com.arka.reporting.application.port.out.persistence.ReportArtifactPersistencePort;
import com.arka.reporting.application.port.out.persistence.ReportingMetricsProjection;
import com.arka.reporting.application.port.out.persistence.ReportingReadPersistencePort;
import com.arka.reporting.application.port.out.persistence.WeeklyReportExecutionPersistencePort;
import com.arka.reporting.application.port.out.security.ActorContext;
import com.arka.reporting.application.port.out.security.ActorContextProviderPort;
import com.arka.reporting.application.query.GetAnalyticFactByIdQuery;
import com.arka.reporting.application.query.GetOperationsKpiQuery;
import com.arka.reporting.application.query.GetReportingAuditQuery;
import com.arka.reporting.application.query.GetReportingMetricsQuery;
import com.arka.reporting.application.query.GetWeeklyExecutionQuery;
import com.arka.reporting.application.query.GetWeeklyReplenishmentProjectionQuery;
import com.arka.reporting.application.query.GetWeeklySalesProjectionQuery;
import com.arka.reporting.application.query.ListReportArtifactsQuery;
import com.arka.reporting.application.query.SearchAnalyticFactsQuery;
import com.arka.reporting.application.result.AnalyticFactResult;
import com.arka.reporting.application.result.FactSearchResult;
import com.arka.reporting.application.result.OperationsKpiResult;
import com.arka.reporting.application.result.ReportArtifactResult;
import com.arka.reporting.application.result.ReplenishmentProjectionResult;
import com.arka.reporting.application.result.ReportingAuditResult;
import com.arka.reporting.application.result.ReportingMetricsResult;
import com.arka.reporting.application.result.SalesProjectionResult;
import com.arka.reporting.application.result.WeeklyExecutionResult;
import com.arka.reporting.domain.analyticfact.aggregate.AnalyticFactAggregate;
import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.analyticfact.enumtype.AnalyticFactStatus;
import com.arka.reporting.domain.analyticfact.enumtype.AnalyticFactType;
import com.arka.reporting.domain.analyticfact.valueobject.FactId;
import com.arka.reporting.domain.analyticfact.valueobject.SourceEventId;
import com.arka.reporting.domain.shared.event.DomainEvent;
import com.arka.reporting.domain.shared.event.ReportingMutationEvent;
import com.arka.reporting.domain.shared.exception.OperationNotPermittedException;
import com.arka.reporting.domain.weeklyreportexecution.aggregate.WeeklyReportExecutionAggregate;
import com.arka.reporting.domain.weeklyreportexecution.entity.OperationsKpiProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.ReportArtifact;
import com.arka.reporting.domain.weeklyreportexecution.entity.ReplenishmentProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.SalesProjection;
import com.arka.reporting.domain.weeklyreportexecution.entity.WeeklyReportExecution;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.ReportType;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.RiskLevel;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.WeeklyReportExecutionStatus;
import com.arka.reporting.domain.weeklyreportexecution.exception.RebuildInProgressException;
import com.arka.reporting.domain.weeklyreportexecution.service.WeekPeriodService;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.ExecutionId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeekId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeeklyReplenishmentReport;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeeklySalesReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReportingApplicationService
        implements RegisterAnalyticFactCommandUseCase,
                ApplyAnalyticFactCommandUseCase,
                UpdateConsumerCheckpointCommandUseCase,
                RebuildProjectionCommandUseCase,
                GenerateWeeklySalesReportCommandUseCase,
                GenerateWeeklyReplenishmentReportCommandUseCase,
                GenerateReportArtifactCommandUseCase,
                ReprocessReportingDlqCommandUseCase,
                GetAnalyticFactByIdQueryUseCase,
                SearchAnalyticFactsQueryUseCase,
                GetWeeklySalesProjectionQueryUseCase,
                GetWeeklyReplenishmentProjectionQueryUseCase,
                GetOperationsKpiQueryUseCase,
                GetWeeklyExecutionQueryUseCase,
                ListReportArtifactsQueryUseCase,
                GetReportingMetricsQueryUseCase,
                GetReportingAuditQueryUseCase {

    private static final String DEFAULT_CONSUMER_NAME = "reporting-service";

    private final AnalyticFactPersistencePort analyticFactPersistencePort;
    private final ProjectionPersistencePort projectionPersistencePort;
    private final WeeklyReportExecutionPersistencePort weeklyReportExecutionPersistencePort;
    private final ReportArtifactPersistencePort reportArtifactPersistencePort;
    private final ConsumerCheckpointPersistencePort consumerCheckpointPersistencePort;
    private final ReportingReadPersistencePort reportingReadPersistencePort;
    private final ProcessedEventPersistencePort processedEventPersistencePort;
    private final ReportingAuditPort reportingAuditPort;
    private final ReportingSearchCachePort reportingSearchCachePort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ActorContextProviderPort actorContextProviderPort;
    private final ActorLegitimacyPort actorLegitimacyPort;
    private final RegionalPolicyPort regionalPolicyPort;
    private final ArtifactStoragePort artifactStoragePort;
    private final DomainEventTopicPort domainEventTopicPort;
    private final ClockPort clockPort;
    private final ReportingResultMapper resultMapper;
    private final ObjectMapper objectMapper;
    private final WeekPeriodService weekPeriodService;
    private final long highLagThreshold;

    public ReportingApplicationService(
            AnalyticFactPersistencePort analyticFactPersistencePort,
            ProjectionPersistencePort projectionPersistencePort,
            WeeklyReportExecutionPersistencePort weeklyReportExecutionPersistencePort,
            ReportArtifactPersistencePort reportArtifactPersistencePort,
            ConsumerCheckpointPersistencePort consumerCheckpointPersistencePort,
            ReportingReadPersistencePort reportingReadPersistencePort,
            ProcessedEventPersistencePort processedEventPersistencePort,
            ReportingAuditPort reportingAuditPort,
            ReportingSearchCachePort reportingSearchCachePort,
            OutboxPersistencePort outboxPersistencePort,
            ActorContextProviderPort actorContextProviderPort,
            ActorLegitimacyPort actorLegitimacyPort,
            RegionalPolicyPort regionalPolicyPort,
            ArtifactStoragePort artifactStoragePort,
            DomainEventTopicPort domainEventTopicPort,
            ClockPort clockPort,
            ReportingResultMapper resultMapper,
            ObjectMapper objectMapper,
            @Value("${app.reporting.rebuild.lag-threshold:10000}") long highLagThreshold) {
        this.analyticFactPersistencePort = analyticFactPersistencePort;
        this.projectionPersistencePort = projectionPersistencePort;
        this.weeklyReportExecutionPersistencePort = weeklyReportExecutionPersistencePort;
        this.reportArtifactPersistencePort = reportArtifactPersistencePort;
        this.consumerCheckpointPersistencePort = consumerCheckpointPersistencePort;
        this.reportingReadPersistencePort = reportingReadPersistencePort;
        this.processedEventPersistencePort = processedEventPersistencePort;
        this.reportingAuditPort = reportingAuditPort;
        this.reportingSearchCachePort = reportingSearchCachePort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.actorContextProviderPort = actorContextProviderPort;
        this.actorLegitimacyPort = actorLegitimacyPort;
        this.regionalPolicyPort = regionalPolicyPort;
        this.artifactStoragePort = artifactStoragePort;
        this.domainEventTopicPort = domainEventTopicPort;
        this.clockPort = clockPort;
        this.resultMapper = resultMapper;
        this.objectMapper = objectMapper;
        this.weekPeriodService = new WeekPeriodService();
        this.highLagThreshold = Math.max(1L, highLagThreshold);
    }

    @Override
    public Mono<AnalyticFactResult> handle(RegisterAnalyticFactCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        String consumerName = normalizeConsumerName(command.consumerName());

        return requireActor(command.tenantId(), true, true)
                .then(checkIdempotency(command.tenantId(), "REGISTER_ANALYTIC_FACT", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findAnalyticFactResult(command.tenantId(), idempotency.targetId());
                    }
                    return analyticFactPersistencePort
                            .findBySourceEventId(
                                    com.arka.reporting.domain.analyticfact.valueobject.TenantId.of(command.tenantId()),
                                    SourceEventId.of(command.sourceEventId()))
                            .map(resultMapper::toResult)
                            .switchIfEmpty(Mono.defer(() -> registerAnalyticFactInternal(command, consumerName, now, payloadHash)));
                });
    }

    private Mono<AnalyticFactResult> registerAnalyticFactInternal(
            RegisterAnalyticFactCommand command,
            String consumerName,
            Instant now,
            String payloadHash) {
        return processedEventPersistencePort
                .exists(command.sourceEventId(), consumerName)
                .flatMap(alreadyProcessed -> {
                    if (alreadyProcessed) {
                        return analyticFactPersistencePort
                                .findBySourceEventId(
                                        com.arka.reporting.domain.analyticfact.valueobject.TenantId.of(command.tenantId()),
                                        SourceEventId.of(command.sourceEventId()))
                                .switchIfEmpty(Mono.error(new ApplicationException(
                                        "evento_procesado_sin_hecho",
                                        "El evento ya fue procesado y no existe analytic_fact asociado")))
                                .map(resultMapper::toResult);
                    }

                    AnalyticFactAggregate aggregate = AnalyticFactAggregate.rehydrate(AnalyticFact.capture(
                            com.arka.reporting.domain.analyticfact.valueobject.TenantId.of(command.tenantId()),
                            SourceEventId.of(command.sourceEventId()),
                            required(command.sourceEventType(), "sourceEventType"),
                            resolveFactType(command.factType()),
                            safePayload(command.payloadJson()),
                            command.occurredAt() == null ? now : command.occurredAt(),
                            now));

                    aggregate.normalize(canonicalizePayload(aggregate.fact().rawPayload()), now);

                    return analyticFactPersistencePort
                            .create(aggregate.fact())
                            .flatMap(created -> processedEventPersistencePort
                                    .record(created.sourceEventId().value(), consumerName, now)
                                    .then(storeDomainEvents(aggregate.pullDomainEvents()))
                                    .then(afterMutation(
                                            command.tenantId(),
                                            "REGISTER_ANALYTIC_FACT",
                                            "AnalyticFact",
                                            created.factId().value(),
                                            command.actorId(),
                                            command.idempotencyKey(),
                                            payloadHash,
                                            payloadJson(Map.of(
                                                    "factId", created.factId().value(),
                                                    "sourceEventId", created.sourceEventId().value())),
                                            new ReportingMutationEvent(
                                                    "AnalyticFactRegistered",
                                                    "AnalyticFact",
                                                    created.factId().value(),
                                                    now)))
                                    .thenReturn(created))
                            .map(resultMapper::toResult);
                });
    }

    @Override
    public Mono<AnalyticFactResult> handle(ApplyAnalyticFactCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true, true)
                .then(checkIdempotency(command.tenantId(), "APPLY_ANALYTIC_FACT", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findAnalyticFactResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadAnalyticFact(command.tenantId(), command.factId())
                            .flatMap(existing -> {
                                if (existing.factStatus() == AnalyticFactStatus.APPLIED) {
                                    return Mono.just(existing);
                                }
                                AnalyticFactAggregate aggregate = AnalyticFactAggregate.rehydrate(existing);
                                if (existing.normalizedPayload() == null || existing.normalizedPayload().isBlank()) {
                                    aggregate.normalize(canonicalizePayload(existing.rawPayload()), now);
                                }
                                aggregate.apply(now);
                                return applyFactToProjections(aggregate.fact())
                                        .then(analyticFactPersistencePort.update(aggregate.fact()))
                                        .flatMap(updated -> storeDomainEvents(aggregate.pullDomainEvents())
                                                .then(afterMutation(
                                                        command.tenantId(),
                                                        "APPLY_ANALYTIC_FACT",
                                                        "AnalyticFact",
                                                        updated.factId().value(),
                                                        command.actorId(),
                                                        command.idempotencyKey(),
                                                        payloadHash,
                                                        payloadJson(Map.of("factId", updated.factId().value())),
                                                        new ReportingMutationEvent(
                                                                "AnalyticFactAppliedMutation",
                                                                "AnalyticFact",
                                                                updated.factId().value(),
                                                                now)))
                                                .thenReturn(updated));
                            })
                            .map(resultMapper::toResult);
                });
    }

    @Override
    public Mono<Void> handle(UpdateConsumerCheckpointCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());

        return requireActor(command.tenantId(), true, false)
                .then(checkIdempotency(command.tenantId(), "UPDATE_CONSUMER_CHECKPOINT", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return Mono.empty();
                    }
                    return consumerCheckpointPersistencePort
                            .upsert(
                                    command.tenantId(),
                                    required(command.consumerName(), "consumerName"),
                                    required(command.topic(), "topic"),
                                    command.partition(),
                                    command.currentOffset(),
                                    command.latestOffset())
                            .flatMap(checkpoint -> {
                                String eventType = checkpoint.lag() > highLagThreshold
                                        ? "ConsumerLagHighDetected"
                                        : "ConsumerCheckpointUpdated";
                                String outcome = checkpoint.lag() > highLagThreshold ? "WARN" : "SUCCESS";
                                return afterMutation(
                                        command.tenantId(),
                                        "UPDATE_CONSUMER_CHECKPOINT",
                                        "ConsumerCheckpoint",
                                        checkpoint.checkpointId(),
                                        command.actorId(),
                                        command.idempotencyKey(),
                                        payloadHash,
                                        payloadJson(Map.of(
                                                "checkpointId", checkpoint.checkpointId(),
                                                "lag", checkpoint.lag())),
                                        new ReportingMutationEvent(
                                                eventType,
                                                "ConsumerCheckpoint",
                                                checkpoint.checkpointId(),
                                                now),
                                        outcome);
                            });
                })
                .then();
    }

    @Override
    public Mono<WeeklyExecutionResult> handle(RebuildProjectionCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        String weekId = resolveWeekId(command.weekId(), now);

        return requireActor(command.tenantId(), true, false)
                .then(checkIdempotency(command.tenantId(), "REBUILD_PROJECTION", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findWeeklyExecutionResult(command.tenantId(), idempotency.targetId());
                    }
                    if (command.fullRebuild()) {
                        return weeklyReportExecutionPersistencePort
                                .existsRunningRebuild(com.arka.reporting.domain.weeklyreportexecution.valueobject.TenantId.of(command.tenantId()))
                                .flatMap(exists -> exists
                                        ? Mono.error(new RebuildInProgressException())
                                        : runRebuild(command, weekId, now, payloadHash));
                    }
                    return runRebuild(command, weekId, now, payloadHash);
                });
    }

    private Mono<WeeklyExecutionResult> runRebuild(
            RebuildProjectionCommand command,
            String weekId,
            Instant now,
            String payloadHash) {
        WeeklyReportExecution initial = WeeklyReportExecution.pending(
                com.arka.reporting.domain.weeklyreportexecution.valueobject.TenantId.of(command.tenantId()),
                WeekId.of(weekId),
                ReportType.FULL_REBUILD,
                now);

        return weeklyReportExecutionPersistencePort
                .create(initial)
                .flatMap(created -> {
                    WeeklyReportExecutionAggregate startAggregate = WeeklyReportExecutionAggregate.rehydrate(created);
                    startAggregate.start(now);
                    return weeklyReportExecutionPersistencePort
                            .update(startAggregate.execution())
                            .flatMap(running -> rebuildProjections(command.tenantId(), weekId, command.fullRebuild())
                                    .then(generateArtifactPayloadForRebuild(command.tenantId(), weekId))
                                    .flatMap(payload -> artifactStoragePort
                                            .store(command.tenantId(), weekId, ReportType.FULL_REBUILD.name(), "JSON", payload)
                                            .flatMap(stored -> createArtifact(running, "JSON", stored, now)
                                                    .flatMap(artifact -> {
                                                        WeeklyReportExecutionAggregate completeAggregate =
                                                                WeeklyReportExecutionAggregate.rehydrate(running);
                                                        completeAggregate.complete(artifact.locationRef(), now);
                                                        return weeklyReportExecutionPersistencePort
                                                                .update(completeAggregate.execution())
                                                                .flatMap(updated -> storeDomainEvents(completeAggregate.pullDomainEvents())
                                                                        .then(afterMutation(
                                                                                command.tenantId(),
                                                                                "REBUILD_PROJECTION",
                                                                                "WeeklyReportExecution",
                                                                                updated.executionId().value(),
                                                                                command.actorId(),
                                                                                command.idempotencyKey(),
                                                                                payloadHash,
                                                                                payloadJson(Map.of(
                                                                                        "executionId", updated.executionId().value(),
                                                                                        "weekId", updated.weekId().value(),
                                                                                        "fullRebuild", command.fullRebuild())),
                                                                                new ReportingMutationEvent(
                                                                                        "ProjectionRebuilt",
                                                                                        "WeeklyReportExecution",
                                                                                        updated.executionId().value(),
                                                                                        now)))
                                                                        .thenReturn(updated));
                                                    })))
                                    .onErrorResume(error -> failExecution(running, error).then(Mono.error(error))));
                })
                .map(resultMapper::toResult);
    }

    private Mono<Void> rebuildProjections(String tenantId, String weekId, boolean fullRebuild) {
        Mono<Void> clearStep = fullRebuild
                ? projectionPersistencePort.clearProjectionsByTenant(tenantId)
                : projectionPersistencePort.clearProjectionsByTenantAndPeriod(tenantId, weekId);

        Flux<AnalyticFact> sourceFacts = fullRebuild
                ? analyticFactPersistencePort.findAppliedByTenant(tenantId)
                : analyticFactPersistencePort
                        .search(new FactSearchFilter(tenantId, null, null, weekId, AnalyticFactStatus.APPLIED.name(), 0, 5000))
                        .flatMap(entry -> analyticFactPersistencePort.findById(
                                com.arka.reporting.domain.analyticfact.valueobject.TenantId.of(tenantId),
                                FactId.of(entry.factId())));

        return clearStep.thenMany(sourceFacts.concatMap(this::applyFactToProjections)).then();
    }

    @Override
    public Mono<WeeklyExecutionResult> handle(GenerateWeeklySalesReportCommand command) {
        Instant now = clockPort.now();
        String weekId = resolveWeekId(command.weekId(), now);
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true, false)
                .then(checkIdempotency(command.tenantId(), "GENERATE_WEEKLY_SALES_REPORT", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findWeeklyExecutionResult(command.tenantId(), idempotency.targetId());
                    }
                    return generateWeeklyReport(
                            command.tenantId(),
                            command.actorId(),
                            weekId,
                            command.format(),
                            command.idempotencyKey(),
                            payloadHash,
                            ReportType.SALES,
                            "GENERATE_WEEKLY_SALES_REPORT");
                });
    }

    @Override
    public Mono<WeeklyExecutionResult> handle(GenerateWeeklyReplenishmentReportCommand command) {
        Instant now = clockPort.now();
        String weekId = resolveWeekId(command.weekId(), now);
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        return requireActor(command.tenantId(), true, false)
                .then(checkIdempotency(command.tenantId(), "GENERATE_WEEKLY_REPLENISHMENT_REPORT", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findWeeklyExecutionResult(command.tenantId(), idempotency.targetId());
                    }
                    return generateWeeklyReport(
                            command.tenantId(),
                            command.actorId(),
                            weekId,
                            command.format(),
                            command.idempotencyKey(),
                            payloadHash,
                            ReportType.REPLENISHMENT,
                            "GENERATE_WEEKLY_REPLENISHMENT_REPORT");
                });
    }

    private Mono<WeeklyExecutionResult> generateWeeklyReport(
            String tenantId,
            String actorId,
            String weekId,
            String format,
            String idempotencyKey,
            String payloadHash,
            ReportType reportType,
            String actionType) {
        com.arka.reporting.domain.weeklyreportexecution.valueobject.TenantId tenant =
                com.arka.reporting.domain.weeklyreportexecution.valueobject.TenantId.of(tenantId);
        WeekId week = WeekId.of(weekId);

        return weeklyReportExecutionPersistencePort
                .findByWeekAndType(tenant, week, reportType.name())
                .map(resultMapper::toResult)
                .switchIfEmpty(Mono.defer(() -> {
                    Instant now = clockPort.now();
                    WeeklyReportExecution pending = WeeklyReportExecution.pending(tenant, week, reportType, now);
                    return weeklyReportExecutionPersistencePort
                            .create(pending)
                            .flatMap(created -> {
                                WeeklyReportExecutionAggregate startAggregate = WeeklyReportExecutionAggregate.rehydrate(created);
                                startAggregate.start(now);
                                return weeklyReportExecutionPersistencePort
                                        .update(startAggregate.execution())
                                        .flatMap(running -> renderWeeklyPayload(tenantId, weekId, reportType)
                                                .flatMap(payload -> artifactStoragePort
                                                        .store(tenantId, weekId, reportType.name(), normalizeFormat(format), payload)
                                                        .flatMap(stored -> createArtifact(running, normalizeFormat(format), stored, now)
                                                                .flatMap(artifact -> {
                                                                    WeeklyReportExecutionAggregate completeAggregate =
                                                                            WeeklyReportExecutionAggregate.rehydrate(running);
                                                                    completeAggregate.complete(artifact.locationRef(), now);
                                                                    return weeklyReportExecutionPersistencePort
                                                                            .update(completeAggregate.execution())
                                                                            .flatMap(updated -> storeDomainEvents(completeAggregate.pullDomainEvents())
                                                                                    .then(afterMutation(
                                                                                            tenantId,
                                                                                            actionType,
                                                                                            "WeeklyReportExecution",
                                                                                            updated.executionId().value(),
                                                                                            actorId,
                                                                                            idempotencyKey,
                                                                                            payloadHash,
                                                                                            payloadJson(Map.of(
                                                                                                    "executionId", updated.executionId().value(),
                                                                                                    "weekId", updated.weekId().value(),
                                                                                                    "reportType", updated.reportType().name())),
                                                                                            new ReportingMutationEvent(
                                                                                                    reportType == ReportType.SALES
                                                                                                            ? "WeeklySalesReportGenerationCompleted"
                                                                                                            : "WeeklyReplenishmentReportGenerationCompleted",
                                                                                                    "WeeklyReportExecution",
                                                                                                    updated.executionId().value(),
                                                                                                    now)))
                                                                                    .thenReturn(updated));
                                                                })))
                                                .onErrorResume(error -> failExecution(running, error).then(Mono.error(error))));
                            })
                            .map(resultMapper::toResult);
                }));
    }

    @Override
    public Mono<ReportArtifactResult> handle(GenerateReportArtifactCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());

        return requireActor(command.tenantId(), true, false)
                .then(checkIdempotency(command.tenantId(), "GENERATE_REPORT_ARTIFACT", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return findArtifactResult(command.tenantId(), idempotency.targetId());
                    }
                    return loadWeeklyExecution(command.tenantId(), command.executionId())
                            .flatMap(execution -> {
                                String format = normalizeFormat(command.format());
                                String payload = command.payload();
                                Mono<String> payloadMono = (payload == null || payload.isBlank())
                                        ? renderWeeklyPayload(command.tenantId(), execution.weekId().value(), execution.reportType())
                                        : Mono.just(payload);
                                return payloadMono
                                        .flatMap(body -> artifactStoragePort
                                                .store(
                                                        command.tenantId(),
                                                        execution.weekId().value(),
                                                        execution.reportType().name(),
                                                        format,
                                                        body))
                                        .flatMap(stored -> createArtifact(execution, format, stored, now))
                                        .flatMap(artifact -> {
                                            Mono<WeeklyReportExecution> maybeComplete;
                                            if (execution.status() == WeeklyReportExecutionStatus.RUNNING) {
                                                WeeklyReportExecutionAggregate aggregate =
                                                        WeeklyReportExecutionAggregate.rehydrate(execution);
                                                aggregate.complete(artifact.locationRef(), now);
                                                maybeComplete = weeklyReportExecutionPersistencePort
                                                        .update(aggregate.execution())
                                                        .flatMap(updated -> storeDomainEvents(aggregate.pullDomainEvents()).thenReturn(updated));
                                            } else {
                                                maybeComplete = Mono.just(execution);
                                            }
                                            return maybeComplete.then(afterMutation(
                                                            command.tenantId(),
                                                            "GENERATE_REPORT_ARTIFACT",
                                                            "ReportArtifact",
                                                            artifact.artifactId(),
                                                            command.actorId(),
                                                            command.idempotencyKey(),
                                                            payloadHash,
                                                            payloadJson(Map.of(
                                                                    "artifactId", artifact.artifactId(),
                                                                    "executionId", artifact.executionId())),
                                                            new ReportingMutationEvent(
                                                                    "ReportArtifactGenerated",
                                                                    "ReportArtifact",
                                                                    artifact.artifactId(),
                                                                    now)))
                                                    .thenReturn(resultMapper.toResult(artifact));
                                        });
                            });
                });
    }

    @Override
    public Mono<Void> handle(ReprocessReportingDlqCommand command) {
        Instant now = clockPort.now();
        String payloadHash = IdempotencySupport.payloadHash(command.toString());
        String consumerName = normalizeConsumerName(command.consumerName());

        return requireActor(command.tenantId(), true, false)
                .then(checkIdempotency(command.tenantId(), "REPROCESS_REPORTING_DLQ", command.idempotencyKey(), payloadHash))
                .flatMap(idempotency -> {
                    if (idempotency.replayed()) {
                        return Mono.empty();
                    }
                    return processedEventPersistencePort
                            .exists(command.dlqEventId(), consumerName)
                            .flatMap(alreadyProcessed -> {
                                if (alreadyProcessed) {
                                    return Mono.empty();
                                }
                                return loadAnalyticFact(command.tenantId(), command.factId())
                                        .flatMap(fact -> {
                                            if (fact.factStatus() != AnalyticFactStatus.APPLIED) {
                                                AnalyticFactAggregate aggregate = AnalyticFactAggregate.rehydrate(fact);
                                                if (fact.normalizedPayload() == null || fact.normalizedPayload().isBlank()) {
                                                    aggregate.normalize(canonicalizePayload(fact.rawPayload()), now);
                                                }
                                                aggregate.apply(now);
                                                return applyFactToProjections(aggregate.fact())
                                                        .then(analyticFactPersistencePort.update(aggregate.fact()))
                                                        .flatMap(updated -> storeDomainEvents(aggregate.pullDomainEvents())
                                                                .thenReturn(updated));
                                            }
                                            return Mono.just(fact);
                                        })
                                        .flatMap(updatedFact -> processedEventPersistencePort
                                                .record(command.dlqEventId(), consumerName, now)
                                                .then(afterMutation(
                                                        command.tenantId(),
                                                        "REPROCESS_REPORTING_DLQ",
                                                        "AnalyticFact",
                                                        updatedFact.factId().value(),
                                                        command.actorId(),
                                                        command.idempotencyKey(),
                                                        payloadHash,
                                                        payloadJson(Map.of(
                                                                "factId", updatedFact.factId().value(),
                                                                "dlqEventId", command.dlqEventId())),
                                                        new ReportingMutationEvent(
                                                                "ReportingDlqReprocessed",
                                                                "AnalyticFact",
                                                                updatedFact.factId().value(),
                                                                now))));
                            });
                })
                .then();
    }

    @Override
    public Mono<AnalyticFactResult> handle(GetAnalyticFactByIdQuery query) {
        return requireActor(query.tenantId(), false, false)
                .then(findAnalyticFactResult(query.tenantId(), query.factId()));
    }

    @Override
    public Mono<FactSearchResult> handle(SearchAnalyticFactsQuery query) {
        return requireActor(query.tenantId(), false, false)
                .then(Mono.defer(() -> {
                    String cacheKey = cacheKeyFor(query);
                    return reportingSearchCachePort
                            .get(cacheKey)
                            .switchIfEmpty(analyticFactPersistencePort
                                    .search(toSearchFilter(query))
                                    .map(resultMapper::toResult)
                                    .collectList()
                                    .zipWith(analyticFactPersistencePort.count(toSearchFilter(query)))
                                    .map(tuple -> new FactSearchResult(
                                            tuple.getT1(),
                                            Math.max(query.page(), 0),
                                            Math.max(query.size(), 1),
                                            tuple.getT2()))
                                    .flatMap(result -> reportingSearchCachePort.put(cacheKey, result).thenReturn(result)));
                }));
    }

    @Override
    public Mono<SalesProjectionResult> handle(GetWeeklySalesProjectionQuery query) {
        return requireActor(query.tenantId(), false, false)
                .then(projectionPersistencePort
                        .findSalesByPeriod(query.tenantId(), required(query.period(), "period"))
                        .switchIfEmpty(Mono.error(new ReportingResourceNotFoundException(
                                "SalesProjection",
                                query.tenantId() + ":" + query.period())))
                        .map(resultMapper::toResult));
    }

    @Override
    public Flux<ReplenishmentProjectionResult> handle(GetWeeklyReplenishmentProjectionQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.max(query.size(), 1);
        return requireActor(query.tenantId(), false, false)
                .thenMany(projectionPersistencePort
                        .findReplenishmentByPeriod(
                                query.tenantId(),
                                required(query.period(), "period"),
                                query.sku(),
                                safePage * safeSize,
                                safeSize)
                        .map(resultMapper::toResult));
    }

    @Override
    public Flux<OperationsKpiResult> handle(GetOperationsKpiQuery query) {
        return requireActor(query.tenantId(), false, false)
                .thenMany(projectionPersistencePort
                        .findOperationsKpisByPeriod(query.tenantId(), required(query.period(), "period"))
                        .map(resultMapper::toResult));
    }

    @Override
    public Mono<WeeklyExecutionResult> handle(GetWeeklyExecutionQuery query) {
        return requireActor(query.tenantId(), false, false)
                .then(Mono.defer(() -> {
                    if (query.executionId() != null && !query.executionId().isBlank()) {
                        return findWeeklyExecutionResult(query.tenantId(), query.executionId());
                    }
                    if (query.weekId() == null || query.weekId().isBlank()
                            || query.reportType() == null || query.reportType().isBlank()) {
                        return Mono.error(new ApplicationException(
                                "query_incompleta",
                                "executionId o (weekId + reportType) son obligatorios"));
                    }
                    return weeklyReportExecutionPersistencePort
                            .findByWeekAndType(
                                    com.arka.reporting.domain.weeklyreportexecution.valueobject.TenantId.of(query.tenantId()),
                                    WeekId.of(query.weekId()),
                                    query.reportType().trim().toUpperCase())
                            .switchIfEmpty(Mono.error(new ReportingResourceNotFoundException(
                                    "WeeklyReportExecution",
                                    query.tenantId() + ":" + query.weekId() + ":" + query.reportType())))
                            .map(resultMapper::toResult);
                }));
    }

    @Override
    public Flux<ReportArtifactResult> handle(ListReportArtifactsQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.max(query.size(), 1);

        return requireActor(query.tenantId(), false, false)
                .thenMany(reportArtifactPersistencePort
                        .findByWeekAndType(
                                query.tenantId(),
                                required(query.weekId(), "weekId"),
                                required(query.reportType(), "reportType").toUpperCase(),
                                safePage * safeSize,
                                safeSize)
                        .map(resultMapper::toResult));
    }

    @Override
    public Mono<ReportingMetricsResult> handle(GetReportingMetricsQuery query) {
        return requireActor(query.tenantId(), true, false)
                .then(reportingReadPersistencePort
                        .metrics(query.tenantId(), query.period())
                        .defaultIfEmpty(new ReportingMetricsProjection(
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                0L,
                                BigDecimal.ZERO,
                                0L))
                        .map(resultMapper::toResult));
    }

    @Override
    public Mono<ReportingAuditResult> handle(GetReportingAuditQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.max(query.size(), 1);

        return requireActor(query.tenantId(), true, false)
                .then(reportingAuditPort
                        .findByTarget(
                                query.tenantId(),
                                query.targetType(),
                                query.targetId(),
                                safePage * safeSize,
                                safeSize)
                        .map(resultMapper::toResult)
                        .collectList()
                        .zipWith(reportingAuditPort.countByTarget(query.tenantId(), query.targetType(), query.targetId()))
                        .map(tuple -> new ReportingAuditResult(tuple.getT1(), safePage, safeSize, tuple.getT2())));
    }

    private Mono<AnalyticFactResult> findAnalyticFactResult(String tenantId, String factId) {
        return loadAnalyticFact(tenantId, factId).map(resultMapper::toResult);
    }

    private Mono<WeeklyExecutionResult> findWeeklyExecutionResult(String tenantId, String executionId) {
        return loadWeeklyExecution(tenantId, executionId).map(resultMapper::toResult);
    }

    private Mono<ReportArtifactResult> findArtifactResult(String tenantId, String artifactId) {
        return reportArtifactPersistencePort
                .findById(tenantId, artifactId)
                .switchIfEmpty(Mono.error(new ReportingResourceNotFoundException("ReportArtifact", artifactId)))
                .map(resultMapper::toResult);
    }

    private Mono<AnalyticFact> loadAnalyticFact(String tenantId, String factId) {
        return analyticFactPersistencePort
                .findById(
                        com.arka.reporting.domain.analyticfact.valueobject.TenantId.of(tenantId),
                        FactId.of(factId))
                .switchIfEmpty(Mono.error(new ReportingResourceNotFoundException("AnalyticFact", factId)));
    }

    private Mono<WeeklyReportExecution> loadWeeklyExecution(String tenantId, String executionId) {
        return weeklyReportExecutionPersistencePort
                .findById(
                        com.arka.reporting.domain.weeklyreportexecution.valueobject.TenantId.of(tenantId),
                        ExecutionId.of(executionId))
                .switchIfEmpty(Mono.error(new ReportingResourceNotFoundException("WeeklyReportExecution", executionId)));
    }

    private Mono<Void> failExecution(WeeklyReportExecution running, Throwable error) {
        WeeklyReportExecutionAggregate failureAggregate = WeeklyReportExecutionAggregate.rehydrate(running);
        failureAggregate.fail("weekly_report_failed", truncate(error.getMessage()), clockPort.now());
        return weeklyReportExecutionPersistencePort
                .update(failureAggregate.execution())
                .flatMap(updated -> afterMutation(
                        updated.tenantId().value(),
                        "WEEKLY_REPORT_FAILED",
                        "WeeklyReportExecution",
                        updated.executionId().value(),
                        "system",
                        null,
                        IdempotencySupport.payloadHash(updated.executionId().value() + updated.errorCode()),
                        payloadJson(Map.of(
                                "executionId", updated.executionId().value(),
                                "error", truncate(error.getMessage()))),
                        new ReportingMutationEvent(
                                "WeeklyReportGenerationFailed",
                                "WeeklyReportExecution",
                                updated.executionId().value(),
                                clockPort.now()),
                        "FAILED"))
                .then();
    }

    private Mono<ReportArtifact> createArtifact(
            WeeklyReportExecution execution,
            String format,
            StoredArtifact stored,
            Instant now) {
        ReportArtifact artifact = new ReportArtifact(
                UUID.randomUUID().toString(),
                execution.executionId().value(),
                execution.tenantId().value(),
                execution.weekId().value(),
                execution.reportType().name(),
                format,
                stored.locationRef(),
                stored.contentHash(),
                stored.sizeBytes(),
                now,
                now);
        return reportArtifactPersistencePort.create(artifact);
    }

    private Mono<String> renderWeeklyPayload(String tenantId, String weekId, ReportType reportType) {
        if (reportType == ReportType.SALES) {
            return projectionPersistencePort
                    .findSalesByPeriod(tenantId, weekId)
                    .defaultIfEmpty(new SalesProjection(
                            "",
                            tenantId,
                            weekId,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            0L,
                            BigDecimal.ZERO,
                            0L,
                            Instant.EPOCH,
                            Instant.EPOCH))
                    .map(projection -> new WeeklySalesReport(
                            tenantId,
                            weekId,
                            projection.totalSales(),
                            projection.paidAmount(),
                            projection.pendingAmount(),
                            projection.confirmedOrders(),
                            projection.averageTicket()))
                    .map(this::toJsonString);
        }

        return projectionPersistencePort
                .findReplenishmentByPeriod(tenantId, weekId, null, 0, 500)
                .collectList()
                .map(items -> {
                    long highRisk = items.stream()
                            .filter(item -> item.riskLevel() == RiskLevel.HIGH || item.riskLevel() == RiskLevel.CRITICAL)
                            .count();
                    List<String> prioritizedSkus = items.stream()
                            .filter(item -> item.riskLevel() == RiskLevel.HIGH || item.riskLevel() == RiskLevel.CRITICAL)
                            .map(ReplenishmentProjection::sku)
                            .limit(20)
                            .toList();
                    return new WeeklyReplenishmentReport(tenantId, weekId, highRisk, prioritizedSkus);
                })
                .map(this::toJsonString);
    }

    private Mono<String> generateArtifactPayloadForRebuild(String tenantId, String weekId) {
        Mono<SalesProjection> sales = projectionPersistencePort
                .findSalesByPeriod(tenantId, weekId)
                .defaultIfEmpty(new SalesProjection(
                        "",
                        tenantId,
                        weekId,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0L,
                        BigDecimal.ZERO,
                        0L,
                        Instant.EPOCH,
                        Instant.EPOCH));

        Mono<Long> replenishCount = projectionPersistencePort.countReplenishmentByPeriod(tenantId, weekId, null);
        Mono<Long> maxLag = consumerCheckpointPersistencePort.maxLagByTenant(tenantId);

        return Mono.zip(sales, replenishCount.defaultIfEmpty(0L), maxLag.defaultIfEmpty(0L))
                .map(tuple -> payloadJson(Map.of(
                        "tenantId", tenantId,
                        "weekId", weekId,
                        "salesTotal", tuple.getT1().totalSales(),
                        "confirmedOrders", tuple.getT1().confirmedOrders(),
                        "replenishmentRows", tuple.getT2(),
                        "maxConsumerLag", tuple.getT3())));
    }

    private Mono<Void> applyFactToProjections(AnalyticFact fact) {
        JsonNode payload = parsePayload(fact);
        String period = resolvePeriod(fact, payload);

        return switch (resolveProjectionType(fact)) {
            case SALES -> applySalesProjection(fact, payload, period);
            case REPLENISHMENT -> applyReplenishmentProjection(fact, payload, period);
            case OPERATIONS, NOTIFICATION, GENERIC -> applyOperationsProjection(fact, payload, period);
        };
    }

    private AnalyticFactType resolveProjectionType(AnalyticFact fact) {
        if (fact.factType() != AnalyticFactType.GENERIC) {
            return fact.factType();
        }
        String eventType = fact.eventType().toLowerCase();
        if (eventType.contains("order") || eventType.contains("payment") || eventType.contains("sale")) {
            return AnalyticFactType.SALES;
        }
        if (eventType.contains("inventory") || eventType.contains("stock") || eventType.contains("replen")) {
            return AnalyticFactType.REPLENISHMENT;
        }
        if (eventType.contains("notification") || eventType.contains("delivery")) {
            return AnalyticFactType.NOTIFICATION;
        }
        return AnalyticFactType.OPERATIONS;
    }

    private Mono<Void> applySalesProjection(AnalyticFact fact, JsonNode payload, String period) {
        BigDecimal totalSales = decimal(payload, "totalSales", "amount", "orderTotal", "total", "value");
        BigDecimal paidAmount = decimal(payload, "paidAmount", "paymentAmount", "paid", "collectedAmount");
        BigDecimal pendingAmount = decimal(payload, "pendingAmount", "dueAmount");

        if (pendingAmount.compareTo(BigDecimal.ZERO) == 0
                && totalSales.compareTo(BigDecimal.ZERO) > 0
                && paidAmount.compareTo(totalSales) < 0) {
            pendingAmount = totalSales.subtract(paidAmount);
        }

        long confirmedOrders = longValue(payload, "confirmedOrders", "orderCount", "orders");
        if (confirmedOrders <= 0 && totalSales.compareTo(BigDecimal.ZERO) > 0) {
            confirmedOrders = 1L;
        }

        return projectionPersistencePort
                .upsertSalesProjection(
                        fact.tenantId().value(),
                        period,
                        totalSales,
                        paidAmount,
                        pendingAmount,
                        confirmedOrders)
                .then();
    }

    private Mono<Void> applyReplenishmentProjection(AnalyticFact fact, JsonNode payload, String period) {
        String sku = text(payload, "sku", "variantSku", "productSku");
        if (sku == null || sku.isBlank()) {
            sku = "UNSPECIFIED-SKU";
        }
        BigDecimal availableQty = decimal(payload, "availableQty", "available", "stockAvailable", "quantity");
        BigDecimal reorderPoint = decimal(payload, "reorderPoint", "minimumQty", "threshold");
        BigDecimal coverageDays = decimal(payload, "coverageDays", "coverage", "daysOfCoverage");
        String risk = resolveRisk(payload, availableQty, reorderPoint, coverageDays).name();

        return projectionPersistencePort
                .upsertReplenishmentProjection(
                        fact.tenantId().value(),
                        period,
                        sku,
                        availableQty,
                        reorderPoint,
                        coverageDays,
                        risk)
                .then();
    }

    private Mono<Void> applyOperationsProjection(AnalyticFact fact, JsonNode payload, String period) {
        String kpiName = text(payload, "kpiName", "metricName", "name");
        if (kpiName == null || kpiName.isBlank()) {
            kpiName = fact.factType() == AnalyticFactType.NOTIFICATION
                    ? "notification_effectiveness"
                    : "generic_events";
        }
        BigDecimal kpiValue = decimal(payload, "kpiValue", "value", "metricValue", "effectiveness");
        if (kpiValue.compareTo(BigDecimal.ZERO) == 0 && "generic_events".equals(kpiName)) {
            kpiValue = BigDecimal.ONE;
        }

        return projectionPersistencePort
                .upsertOperationsKpiProjection(fact.tenantId().value(), period, kpiName, kpiValue)
                .then();
    }

    private Mono<Void> storeDomainEvents(List<DomainEvent> domainEvents) {
        return Flux.fromIterable(domainEvents)
                .concatMap(event -> outboxPersistencePort.store(event, domainEventPayload(event)))
                .then();
    }

    private Mono<Void> requireActor(String tenantId, boolean adminRequired, boolean regionalPolicyRequired) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.error(new ApplicationException("tenant_requerido", "tenantId es obligatorio"));
        }

        return actorContextProviderPort
                .currentActor()
                .switchIfEmpty(Mono.error(new OperationNotPermittedException(
                        "actor_no_autenticado",
                        "No hay actor autenticado disponible para ejecutar la operacion")))
                .flatMap(actor -> {
                    if (adminRequired && !actor.admin() && !actor.trustedService()) {
                        return Mono.error(new OperationNotPermittedException(
                                "operacion_no_permitida",
                                "La operacion requiere rol administrativo o servicio tecnico"));
                    }
                    if (!actor.admin() && !actor.trustedService() && !tenantId.equals(actor.tenantId())) {
                        return Mono.error(new ApplicationException(
                                "acceso_cross_tenant",
                                "Actor no autorizado para el tenant solicitado"));
                    }
                    if (actor.trustedService()) {
                        return Mono.just(actor);
                    }
                    return actorLegitimacyPort
                            .isLegitimate(actor.actorId(), tenantId)
                            .flatMap(valid -> valid ? Mono.just(actor) : Mono.error(new ActorNotLegitimateException()));
                })
                .flatMap(actor -> {
                    if (!regionalPolicyRequired) {
                        return Mono.empty();
                    }
                    String countryCode = actor.countryCode() == null || actor.countryCode().isBlank()
                            ? "GLOBAL"
                            : actor.countryCode();
                    return regionalPolicyPort
                            .resolveForOperation(tenantId, countryCode)
                            .filter(policy -> policy != null && policy.available())
                            .switchIfEmpty(Mono.error(new RegionalPolicyUnavailableException()))
                            .then();
                });
    }

    private Mono<IdempotencyDecision> checkIdempotency(
            String tenantId,
            String actionType,
            String idempotencyKey,
            String payloadHash) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.just(IdempotencyDecision.none());
        }

        return reportingAuditPort
                .findByIdempotency(tenantId, actionType, idempotencyKey)
                .flatMap(existing -> {
                    if (!payloadHash.equals(existing.payloadHash())) {
                        return Mono.error(new IdempotencyConflictException());
                    }
                    return Mono.just(new IdempotencyDecision(true, existing.targetId()));
                })
                .switchIfEmpty(Mono.just(IdempotencyDecision.none()));
    }

    private Mono<Void> afterMutation(
            String tenantId,
            String actionType,
            String targetType,
            String targetId,
            String actorId,
            String idempotencyKey,
            String payloadHash,
            String payload,
            DomainEvent mutationEvent) {
        return afterMutation(
                tenantId,
                actionType,
                targetType,
                targetId,
                actorId,
                idempotencyKey,
                payloadHash,
                payload,
                mutationEvent,
                "SUCCESS");
    }

    private Mono<Void> afterMutation(
            String tenantId,
            String actionType,
            String targetType,
            String targetId,
            String actorId,
            String idempotencyKey,
            String payloadHash,
            String payload,
            DomainEvent mutationEvent,
            String outcome) {
        ReportingAuditEntry auditEntry = new ReportingAuditEntry(
                UUID.randomUUID().toString(),
                tenantId,
                actorId == null || actorId.isBlank() ? "system" : actorId,
                actionType,
                targetType,
                targetId,
                outcome,
                payload,
                normalizeIdempotencyKey(idempotencyKey),
                payloadHash,
                clockPort.now());

        return reportingAuditPort
                .record(auditEntry)
                .then(outboxPersistencePort.store(mutationEvent, domainEventPayload(mutationEvent)))
                .then(reportingSearchCachePort.evictTenant(tenantId));
    }

    private String domainEventPayload(DomainEvent event) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("eventId", event.eventId());
        root.put("eventType", event.eventType());
        root.put("aggregateType", event.aggregateType());
        root.put("aggregateId", event.aggregateId());
        root.put("occurredAt", event.occurredAt().toString());
        root.put("topic", domainEventTopicPort.topicFor(event.eventType()));
        root.set("data", safeDomainEventData(event));
        return toJsonString(root);
    }

    private JsonNode safeDomainEventData(DomainEvent event) {
        try {
            JsonNode data = objectMapper.valueToTree(event);
            return data == null || data.isNull() ? objectMapper.createObjectNode() : data;
        } catch (IllegalArgumentException exception) {
            return objectMapper.createObjectNode();
        }
    }

    private String cacheKeyFor(SearchAnalyticFactsQuery query) {
        return query.tenantId() + "::" + safe(query.eventType()) + "::" + safe(query.factType()) + "::"
                + safe(query.period()) + "::" + safe(query.status()) + "::" + query.page() + "::" + query.size();
    }

    private FactSearchFilter toSearchFilter(SearchAnalyticFactsQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.max(query.size(), 1);
        return new FactSearchFilter(
                query.tenantId(),
                query.eventType(),
                query.factType(),
                query.period(),
                query.status(),
                safePage * safeSize,
                safeSize);
    }

    private String normalizeConsumerName(String consumerName) {
        if (consumerName == null || consumerName.isBlank()) {
            return DEFAULT_CONSUMER_NAME;
        }
        return consumerName.trim();
    }

    private String normalizeFormat(String format) {
        if (format == null || format.isBlank()) {
            return "JSON";
        }
        return format.trim().toUpperCase();
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String resolveWeekId(String weekId, Instant now) {
        if (weekId == null || weekId.isBlank()) {
            return weekPeriodService.weekIdFrom(now);
        }
        return WeekId.of(weekId.trim()).value();
    }

    private AnalyticFactType resolveFactType(String factType) {
        if (factType == null || factType.isBlank()) {
            return AnalyticFactType.GENERIC;
        }
        return AnalyticFactType.valueOf(factType.trim().toUpperCase());
    }

    private String canonicalizePayload(String payload) {
        try {
            JsonNode parsed = objectMapper.readTree(safePayload(payload));
            return objectMapper.writeValueAsString(parsed);
        } catch (Exception exception) {
            ObjectNode fallback = objectMapper.createObjectNode();
            fallback.put("raw", safePayload(payload));
            return toJsonString(fallback);
        }
    }

    private JsonNode parsePayload(AnalyticFact fact) {
        String json = fact.normalizedPayload();
        if (json == null || json.isBlank()) {
            json = fact.rawPayload();
        }
        try {
            return objectMapper.readTree(safePayload(json));
        } catch (Exception exception) {
            return objectMapper.createObjectNode();
        }
    }

    private String resolvePeriod(AnalyticFact fact, JsonNode payload) {
        String payloadPeriod = text(payload, "period", "weekId", "week", "businessWeek");
        if (payloadPeriod != null && !payloadPeriod.isBlank()) {
            try {
                return WeekId.of(payloadPeriod).value();
            } catch (Exception ignored) {
                return weekPeriodService.weekIdFrom(fact.occurredAt() == null ? clockPort.now() : fact.occurredAt());
            }
        }
        return weekPeriodService.weekIdFrom(fact.occurredAt() == null ? clockPort.now() : fact.occurredAt());
    }

    private BigDecimal decimal(JsonNode payload, String... names) {
        for (String name : names) {
            JsonNode candidate = payload.get(name);
            if (candidate == null || candidate.isNull()) {
                continue;
            }
            try {
                return new BigDecimal(candidate.asText("0")).setScale(2, RoundingMode.HALF_UP);
            } catch (Exception ignored) {
                // Continue with next candidate
            }
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private long longValue(JsonNode payload, String... names) {
        for (String name : names) {
            JsonNode candidate = payload.get(name);
            if (candidate == null || candidate.isNull()) {
                continue;
            }
            try {
                return Long.parseLong(candidate.asText("0"));
            } catch (Exception ignored) {
                // Continue with next candidate
            }
        }
        return 0L;
    }

    private String text(JsonNode payload, String... names) {
        for (String name : names) {
            JsonNode candidate = payload.get(name);
            if (candidate == null || candidate.isNull()) {
                continue;
            }
            String value = candidate.asText("").trim();
            if (!value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private RiskLevel resolveRisk(
            JsonNode payload,
            BigDecimal availableQty,
            BigDecimal reorderPoint,
            BigDecimal coverageDays) {
        String explicitRisk = text(payload, "riskLevel", "risk");
        if (explicitRisk != null) {
            try {
                return RiskLevel.valueOf(explicitRisk.toUpperCase());
            } catch (Exception ignored) {
                // fallback to rule based risk
            }
        }

        if (availableQty.compareTo(reorderPoint) < 0 || coverageDays.compareTo(new BigDecimal("2.00")) < 0) {
            return RiskLevel.CRITICAL;
        }
        if (coverageDays.compareTo(new BigDecimal("5.00")) < 0) {
            return RiskLevel.HIGH;
        }
        if (coverageDays.compareTo(new BigDecimal("10.00")) < 0) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private String payloadJson(Map<String, ?> values) {
        return toJsonString(values);
    }

    private String toJsonString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new ApplicationException("serializacion_invalida", "No fue posible serializar payload");
        }
    }

    private String safePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return "{}";
        }
        return payload.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ApplicationException("campo_requerido", field + " es obligatorio");
        }
        return value.trim();
    }

    private String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "sin_detalle";
        }
        String trimmed = message.trim();
        return trimmed.length() <= 512 ? trimmed : trimmed.substring(0, 512);
    }

    private record IdempotencyDecision(boolean replayed, String targetId) {

        static IdempotencyDecision none() {
            return new IdempotencyDecision(false, "");
        }
    }
}
