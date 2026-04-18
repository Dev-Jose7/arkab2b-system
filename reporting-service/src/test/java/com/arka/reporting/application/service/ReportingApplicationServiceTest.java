package com.arka.reporting.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.reporting.application.command.ApplyAnalyticFactCommand;
import com.arka.reporting.application.command.RegisterAnalyticFactCommand;
import com.arka.reporting.application.command.RebuildProjectionCommand;
import com.arka.reporting.application.command.ReprocessReportingDlqCommand;
import com.arka.reporting.application.mapper.result.ReportingResultMapper;
import com.arka.reporting.application.port.out.audit.ReportingAuditPort;
import com.arka.reporting.application.port.out.cache.ReportingSearchCachePort;
import com.arka.reporting.application.port.out.directory.RegionalPolicyPort;
import com.arka.reporting.application.port.out.directory.RegionalPolicyResolution;
import com.arka.reporting.application.port.out.event.DomainEventTopicPort;
import com.arka.reporting.application.port.out.external.ActorLegitimacyPort;
import com.arka.reporting.application.port.out.external.ArtifactStoragePort;
import com.arka.reporting.application.port.out.external.ClockPort;
import com.arka.reporting.application.port.out.external.StoredArtifact;
import com.arka.reporting.application.port.out.persistence.AnalyticFactPersistencePort;
import com.arka.reporting.application.port.out.persistence.ConsumerCheckpointPersistencePort;
import com.arka.reporting.application.port.out.persistence.OutboxPersistencePort;
import com.arka.reporting.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.reporting.application.port.out.persistence.ProjectionPersistencePort;
import com.arka.reporting.application.port.out.persistence.ReportArtifactPersistencePort;
import com.arka.reporting.application.port.out.persistence.ReportingReadPersistencePort;
import com.arka.reporting.application.port.out.persistence.WeeklyReportExecutionPersistencePort;
import com.arka.reporting.application.port.out.security.ActorContext;
import com.arka.reporting.application.port.out.security.ActorContextProviderPort;
import com.arka.reporting.application.query.GetAnalyticFactByIdQuery;
import com.arka.reporting.domain.analyticfact.entity.AnalyticFact;
import com.arka.reporting.domain.analyticfact.enumtype.AnalyticFactType;
import com.arka.reporting.domain.analyticfact.valueobject.SourceEventId;
import com.arka.reporting.domain.analyticfact.valueobject.TenantId;
import com.arka.reporting.domain.shared.exception.OperationNotPermittedException;
import com.arka.reporting.domain.weeklyreportexecution.exception.RebuildInProgressException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportingApplicationServiceTest {

    @Mock
    private AnalyticFactPersistencePort analyticFactPersistencePort;

    @Mock
    private ProjectionPersistencePort projectionPersistencePort;

    @Mock
    private WeeklyReportExecutionPersistencePort weeklyReportExecutionPersistencePort;

    @Mock
    private ReportArtifactPersistencePort reportArtifactPersistencePort;

    @Mock
    private ConsumerCheckpointPersistencePort consumerCheckpointPersistencePort;

    @Mock
    private ReportingReadPersistencePort reportingReadPersistencePort;

    @Mock
    private ProcessedEventPersistencePort processedEventPersistencePort;

    @Mock
    private ReportingAuditPort reportingAuditPort;

    @Mock
    private ReportingSearchCachePort reportingSearchCachePort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private ActorContextProviderPort actorContextProviderPort;

    @Mock
    private ActorLegitimacyPort actorLegitimacyPort;

    @Mock
    private RegionalPolicyPort regionalPolicyPort;

    @Mock
    private ArtifactStoragePort artifactStoragePort;

    @Mock
    private DomainEventTopicPort domainEventTopicPort;

    @Mock
    private ClockPort clockPort;

    private ReportingApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ReportingApplicationService(
                analyticFactPersistencePort,
                projectionPersistencePort,
                weeklyReportExecutionPersistencePort,
                reportArtifactPersistencePort,
                consumerCheckpointPersistencePort,
                reportingReadPersistencePort,
                processedEventPersistencePort,
                reportingAuditPort,
                reportingSearchCachePort,
                outboxPersistencePort,
                actorContextProviderPort,
                actorLegitimacyPort,
                regionalPolicyPort,
                artifactStoragePort,
                domainEventTopicPort,
                clockPort,
                new ReportingResultMapper(),
                new ObjectMapper(),
                10000L);
    }

    @Test
    void shouldReturnExistingFactWhenSourceEventAlreadyExists() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        RegisterAnalyticFactCommand command = new RegisterAnalyticFactCommand(
                "tenant-demo",
                "actor-1",
                "evt-1",
                "order.confirmed",
                "SALES",
                "{}",
                now,
                "consumer-reporting",
                null);

        AnalyticFact existing = capturedFact(now, "evt-1");

        mockAdminActor(true);
        when(clockPort.now()).thenReturn(now);
        when(analyticFactPersistencePort.findBySourceEventId(any(), any())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.handle(command))
                .assertNext(result -> assertEquals(existing.factId().value(), result.factId()))
                .verifyComplete();

        verify(analyticFactPersistencePort, never()).create(any());
    }

    @Test
    void shouldSkipApplyWhenFactAlreadyApplied() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        ApplyAnalyticFactCommand command = new ApplyAnalyticFactCommand(
                "tenant-demo",
                "actor-1",
                "fact-1",
                null);

        AnalyticFact applied = capturedFact(now, "evt-2");
        applied.normalize("{}", now.plusSeconds(1));
        applied.apply(now.plusSeconds(2));

        mockAdminActor(true);
        when(clockPort.now()).thenReturn(now.plusSeconds(3));
        when(analyticFactPersistencePort.findById(any(), any())).thenReturn(Mono.just(applied));

        StepVerifier.create(service.handle(command))
                .assertNext(result -> assertEquals("APPLIED", result.factStatus()))
                .verifyComplete();

        verify(analyticFactPersistencePort, never()).update(any());
        verify(projectionPersistencePort, never()).upsertSalesProjection(any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    void shouldKeepDlqReprocessIdempotentWhenEventAlreadyProcessed() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        ReprocessReportingDlqCommand command = new ReprocessReportingDlqCommand(
                "tenant-demo",
                "actor-1",
                "dlq-evt-1",
                "reporting-dlq-consumer",
                "fact-1",
                null);

        mockAdminActor(false);
        when(clockPort.now()).thenReturn(now);
        when(processedEventPersistencePort.exists("dlq-evt-1", "reporting-dlq-consumer")).thenReturn(Mono.just(Boolean.TRUE));

        StepVerifier.create(service.handle(command)).verifyComplete();

        verify(analyticFactPersistencePort, never()).findById(any(), any());
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        when(actorContextProviderPort.currentActor()).thenReturn(Mono.empty());
        when(analyticFactPersistencePort.findById(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(new GetAnalyticFactByIdQuery("tenant-demo", "fact-1")))
                .expectError(OperationNotPermittedException.class)
                .verify();
    }

    @Test
    void shouldRejectFullRebuildWhenAnotherRebuildIsRunning() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        RebuildProjectionCommand command = new RebuildProjectionCommand(
                "tenant-demo",
                "actor-1",
                true,
                "2026-W14",
                null);

        mockAdminActor(false);
        when(clockPort.now()).thenReturn(now);
        when(weeklyReportExecutionPersistencePort.existsRunningRebuild(any())).thenReturn(Mono.just(true));

        StepVerifier.create(service.handle(command))
                .expectError(RebuildInProgressException.class)
                .verify();
    }

    private void mockAdminActor(boolean withRegionalPolicy) {
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext("actor-1", "tenant-demo", "CO", true, false)));
        when(actorLegitimacyPort.isLegitimate("actor-1", "tenant-demo")).thenReturn(Mono.just(Boolean.TRUE));
        when(domainEventTopicPort.topicFor(any())).thenReturn("reporting.mutation.v1");
        when(reportingSearchCachePort.evictTenant(any())).thenReturn(Mono.empty());
        when(reportingAuditPort.record(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any(), any())).thenReturn(Mono.empty());
        when(processedEventPersistencePort.record(any(), any(), any())).thenReturn(Mono.empty());
        when(artifactStoragePort.store(any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(new StoredArtifact("stub://artifact", "hash", 100L)));
        if (withRegionalPolicy) {
            when(regionalPolicyPort.resolveForOperation("tenant-demo", "CO"))
                    .thenReturn(Mono.just(new RegionalPolicyResolution("tenant-demo", "CO", true, "policy-co")));
        }
    }

    private AnalyticFact capturedFact(Instant now, String sourceEventId) {
        return AnalyticFact.capture(
                TenantId.of("tenant-demo"),
                SourceEventId.of(sourceEventId),
                "order.confirmed",
                AnalyticFactType.SALES,
                "{}",
                now,
                now);
    }
}
