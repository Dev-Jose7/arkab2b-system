package com.arka.notification.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.notification.application.command.DispatchNotificationCommand;
import com.arka.notification.application.command.EmitRelevantChangeNotificationCommand;
import com.arka.notification.application.command.ProcessProviderCallbackCommand;
import com.arka.notification.application.command.RetryNotificationCommand;
import com.arka.notification.application.mapper.result.NotificationResultMapper;
import com.arka.notification.application.port.out.audit.NotificationAuditEntry;
import com.arka.notification.application.port.out.audit.NotificationAuditPort;
import com.arka.notification.application.port.out.cache.NotificationSearchCachePort;
import com.arka.notification.application.port.out.directory.RecipientResolution;
import com.arka.notification.application.port.out.directory.RecipientResolverPort;
import com.arka.notification.application.port.out.event.DomainEventTopicPort;
import com.arka.notification.application.port.out.external.ActorLegitimacyPort;
import com.arka.notification.application.port.out.external.ClockPort;
import com.arka.notification.application.port.out.external.NotificationProviderPort;
import com.arka.notification.application.port.out.external.ProviderSendResult;
import com.arka.notification.application.port.out.external.TemplateRendererPort;
import com.arka.notification.application.port.out.persistence.NotificationAttemptPersistencePort;
import com.arka.notification.application.port.out.persistence.NotificationReadPersistencePort;
import com.arka.notification.application.port.out.persistence.NotificationRequestPersistencePort;
import com.arka.notification.application.port.out.persistence.NotificationTemplatePolicyPersistencePort;
import com.arka.notification.application.port.out.persistence.OutboxPersistencePort;
import com.arka.notification.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.notification.application.port.out.persistence.ProviderCallbackPersistencePort;
import com.arka.notification.application.port.out.persistence.ProviderCallbackProjection;
import com.arka.notification.application.port.out.security.ActorContext;
import com.arka.notification.application.port.out.security.ActorContextProviderPort;
import com.arka.notification.domain.notificationdispatch.entity.ChannelPolicy;
import com.arka.notification.domain.notificationdispatch.entity.NotificationAttempt;
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.entity.NotificationTemplate;
import com.arka.notification.domain.notificationdispatch.entity.ProviderCallback;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationAttemptResultStatus;
import com.arka.notification.domain.notificationdispatch.enumtype.ProviderCallbackStatus;
import com.arka.notification.domain.notificationdispatch.exception.DiscardedNotificationCannotDispatchException;
import com.arka.notification.domain.notificationdispatch.valueobject.AttemptId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
import com.arka.notification.domain.shared.exception.OperationNotPermittedException;
import java.time.Instant;
import org.springframework.dao.DuplicateKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @Mock
    private NotificationRequestPersistencePort requestPersistencePort;

    @Mock
    private NotificationAttemptPersistencePort attemptPersistencePort;

    @Mock
    private NotificationTemplatePolicyPersistencePort templatePolicyPersistencePort;

    @Mock
    private ProviderCallbackPersistencePort providerCallbackPersistencePort;

    @Mock
    private NotificationReadPersistencePort readPersistencePort;

    @Mock
    private ProcessedEventPersistencePort processedEventPersistencePort;

    @Mock
    private NotificationAuditPort notificationAuditPort;

    @Mock
    private NotificationSearchCachePort notificationSearchCachePort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private RecipientResolverPort recipientResolverPort;

    @Mock
    private TemplateRendererPort templateRendererPort;

    @Mock
    private NotificationProviderPort notificationProviderPort;

    @Mock
    private DomainEventTopicPort domainEventTopicPort;

    @Mock
    private ActorContextProviderPort actorContextProviderPort;

    @Mock
    private ActorLegitimacyPort actorLegitimacyPort;

    @Mock
    private ClockPort clockPort;

    private NotificationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationApplicationService(
                requestPersistencePort,
                attemptPersistencePort,
                templatePolicyPersistencePort,
                providerCallbackPersistencePort,
                readPersistencePort,
                processedEventPersistencePort,
                notificationAuditPort,
                notificationSearchCachePort,
                outboxPersistencePort,
                recipientResolverPort,
                templateRendererPort,
                notificationProviderPort,
                domainEventTopicPort,
                actorContextProviderPort,
                actorLegitimacyPort,
                clockPort,
                new NotificationResultMapper());
    }

    @Test
    void shouldReturnExistingNotificationWhenDedupeKeyAlreadyExists() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        EmitRelevantChangeNotificationCommand command = new EmitRelevantChangeNotificationCommand(
                "organization-demo",
                "actor-1",
                "evt-1",
                "order.confirmed",
                "recipient-1",
                "EMAIL",
                "{}",
                "trace-1",
                "corr-1",
                null);

        NotificationRequest existing = pendingRequest(now);

        mockAdminActor();
        when(clockPort.now()).thenReturn(now);
        when(requestPersistencePort.findByKey(any(), any())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.handle(command))
                .assertNext(result -> assertEquals(existing.notificationId().value(), result.notificationId()))
                .verifyComplete();

        verify(requestPersistencePort, never()).create(any());
    }

    @Test
    void shouldRejectRetryWhenNotificationIsDiscarded() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        RetryNotificationCommand command = new RetryNotificationCommand(
                "organization-demo",
                "actor-1",
                "noti-1",
                null);

        NotificationRequest discarded = pendingRequest(now);
        discarded.discard(now.plusSeconds(1));

        ChannelPolicy policy = new ChannelPolicy(
                "policy-1",
                "organization-demo",
                "order.confirmed",
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                3,
                60,
                true);

        mockAdminActor();
        when(clockPort.now()).thenReturn(now.plusSeconds(2));
        when(requestPersistencePort.findById(any(), any())).thenReturn(Mono.just(discarded));
        when(attemptPersistencePort.findByNotificationId(any(), any())).thenReturn(Flux.empty());
        when(templatePolicyPersistencePort.findActivePolicy("organization-demo", "order.confirmed")).thenReturn(Mono.just(policy));

        StepVerifier.create(service.handle(command))
                .expectError(DiscardedNotificationCannotDispatchException.class)
                .verify();

        verify(attemptPersistencePort, never()).create(any(), any());
    }

    @Test
    void shouldTreatDuplicatedProviderCallbackAsIdempotentNoop() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        ProcessProviderCallbackCommand command = new ProcessProviderCallbackCommand(
                "organization-demo",
                "actor-1",
                "noti-1",
                "stub-provider",
                "provider-ref-1",
                "cb-event-1",
                "VALIDATED",
                "{}",
                null);

        NotificationRequest request = pendingRequest(now);
        ProviderCallback existing = new ProviderCallback(
                "callback-1",
                "organization-demo",
                "noti-1",
                "stub-provider",
                "provider-ref-1",
                "cb-event-1",
                ProviderCallbackStatus.VALIDATED,
                "{}",
                now,
                now);

        ProviderCallbackProjection projection = new ProviderCallbackProjection(
                "callback-1",
                "noti-1",
                "stub-provider",
                "provider-ref-1",
                "cb-event-1",
                "VALIDATED",
                "{}",
                now,
                now);

        mockAdminActor();
        when(clockPort.now()).thenReturn(now.plusSeconds(5));
        when(providerCallbackPersistencePort.findByProviderRefAndEvent("stub-provider", "provider-ref-1", "cb-event-1"))
                .thenReturn(Mono.just(existing));
        when(requestPersistencePort.findById(any(), any())).thenReturn(Mono.just(request));
        when(attemptPersistencePort.findByNotificationId(any(), any())).thenReturn(Flux.empty());
        when(providerCallbackPersistencePort.findByNotificationId("organization-demo", "noti-1"))
                .thenReturn(Flux.just(projection));

        StepVerifier.create(service.handle(command))
                .assertNext(detail -> {
                    assertEquals("noti-1", detail.notification().notificationId());
                    assertEquals(1, detail.callbacks().size());
                })
                .verifyComplete();
    }

    @Test
    void shouldKeepRequestRetryableOnFallbackWhenProviderFailureIsNonRetryable() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        DispatchNotificationCommand command = new DispatchNotificationCommand(
                "organization-demo",
                "actor-1",
                "noti-1",
                null);

        NotificationRequest request = pendingRequest(now);
        ChannelPolicy policy = new ChannelPolicy(
                "policy-1",
                "organization-demo",
                "order.confirmed",
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                3,
                60,
                true);

        mockAdminActor();
        when(clockPort.now()).thenReturn(now.plusSeconds(1));
        when(notificationAuditPort.record(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any(), any())).thenReturn(Mono.empty());
        when(notificationSearchCachePort.evictOrganization(any())).thenReturn(Mono.empty());

        when(requestPersistencePort.findById(any(), any())).thenReturn(Mono.just(request));
        when(requestPersistencePort.update(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(templatePolicyPersistencePort.findActivePolicy("organization-demo", "order.confirmed")).thenReturn(Mono.just(policy));
        when(attemptPersistencePort.create(any(), any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(attemptPersistencePort.update(any(), any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(attemptPersistencePort.findByNotificationId(any(), any())).thenReturn(Flux.empty());
        when(providerCallbackPersistencePort.findByNotificationId("organization-demo", "noti-1")).thenReturn(Flux.empty());
        when(recipientResolverPort.resolve("organization-demo", "recipient-1", "EMAIL"))
                .thenReturn(Mono.just(new RecipientResolution("recipient-1", "EMAIL", "test@example.com", true)));
        when(notificationProviderPort.send(any()))
                .thenReturn(Mono.just(new ProviderSendResult(
                        false,
                        null,
                        "PROVIDER_DOWN",
                        "provider unavailable",
                        250L,
                        false,
                        "{}")));

        StepVerifier.create(service.handle(command))
                .assertNext(detail -> {
                    assertEquals("FAILED", detail.notification().status());
                    assertEquals(true, detail.notification().retryable());
                })
                .verifyComplete();
    }

    @Test
    void shouldSynchronizeAttemptCountFromPersistedAttemptsBeforeDispatchingAgain() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        DispatchNotificationCommand command = new DispatchNotificationCommand(
                "organization-demo",
                "actor-1",
                "noti-1",
                null);

        NotificationRequest request = pendingRequest(now);
        request.synchronizeAttemptCount(5, now);
        NotificationAttempt persistedAttempt = NotificationAttempt.rehydrate(
                AttemptId.of("attempt-1"),
                NotificationId.of("noti-1"),
                1,
                NotificationAttemptResultStatus.CREATED,
                "EMAIL",
                null,
                null,
                null,
                true,
                null,
                request.payloadJson(),
                null,
                now.minusSeconds(5));
        ChannelPolicy policy = new ChannelPolicy(
                "policy-1",
                "organization-demo",
                "order.confirmed",
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                3,
                60,
                true);

        mockAdminActor();
        when(clockPort.now()).thenReturn(now.plusSeconds(1));
        when(notificationAuditPort.record(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any(), any())).thenReturn(Mono.empty());
        when(notificationSearchCachePort.evictOrganization(any())).thenReturn(Mono.empty());
        when(requestPersistencePort.findById(any(), any())).thenReturn(Mono.just(request));
        when(requestPersistencePort.update(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(templatePolicyPersistencePort.findActivePolicy("organization-demo", "order.confirmed")).thenReturn(Mono.just(policy));
        when(attemptPersistencePort.findByNotificationId(any(), any())).thenReturn(Flux.just(persistedAttempt));
        when(attemptPersistencePort.create(any(), any())).thenAnswer(invocation -> {
            NotificationAttempt created = invocation.getArgument(0);
            assertEquals(2, created.attemptNumber());
            return Mono.just(created);
        });
        when(attemptPersistencePort.update(any(), any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(providerCallbackPersistencePort.findByNotificationId("organization-demo", "noti-1")).thenReturn(Flux.empty());
        when(recipientResolverPort.resolve("organization-demo", "recipient-1", "EMAIL"))
                .thenReturn(Mono.just(new RecipientResolution("recipient-1", "EMAIL", "test@example.com", true)));
        when(notificationProviderPort.send(any()))
                .thenReturn(Mono.just(new ProviderSendResult(
                        true,
                        "provider-ref-2",
                        null,
                        null,
                        120L,
                        false,
                        "{}")));

        StepVerifier.create(service.handle(command))
                .assertNext(detail -> {
                    assertEquals("SENT", detail.notification().status());
                    assertEquals(2, detail.notification().attemptCount());
                })
                .verifyComplete();
    }

    @Test
    void shouldPersistDiscardWhenPersistedAttemptsAlreadyExhausted() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        DispatchNotificationCommand command = new DispatchNotificationCommand(
                "organization-demo",
                "actor-1",
                "noti-1",
                null);

        NotificationRequest request = pendingRequest(now);
        NotificationAttempt persistedAttempt = NotificationAttempt.rehydrate(
                AttemptId.of("attempt-3"),
                NotificationId.of("noti-1"),
                3,
                NotificationAttemptResultStatus.FAILED,
                "EMAIL",
                null,
                "DESTINATION_MISSING",
                "recipient missing",
                false,
                null,
                request.payloadJson(),
                "{}",
                now.minusSeconds(5));
        ChannelPolicy policy = new ChannelPolicy(
                "policy-1",
                "organization-demo",
                "order.confirmed",
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                3,
                60,
                true);

        mockAdminActor();
        when(clockPort.now()).thenReturn(now.plusSeconds(1));
        when(requestPersistencePort.findById(any(), any())).thenReturn(Mono.just(request));
        when(requestPersistencePort.update(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(templatePolicyPersistencePort.findActivePolicy("organization-demo", "order.confirmed")).thenReturn(Mono.just(policy));
        when(attemptPersistencePort.findByNotificationId(any(), any())).thenReturn(Flux.just(persistedAttempt));

        StepVerifier.create(service.handle(command))
                .expectError(DiscardedNotificationCannotDispatchException.class)
                .verify();

        verify(requestPersistencePort).update(any());
        verify(attemptPersistencePort, never()).create(any(), any());
    }

    @Test
    void shouldDiscardNotificationWhenRecipientCannotBeResolved() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        DispatchNotificationCommand command = new DispatchNotificationCommand(
                "organization-demo",
                "actor-1",
                "noti-1",
                null);

        NotificationRequest request = pendingRequest(now);
        ChannelPolicy policy = new ChannelPolicy(
                "policy-1",
                "organization-demo",
                "order.confirmed",
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                3,
                60,
                true);

        mockAdminActor();
        when(clockPort.now()).thenReturn(now.plusSeconds(1));
        when(notificationAuditPort.record(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any(), any())).thenReturn(Mono.empty());
        when(notificationSearchCachePort.evictOrganization(any())).thenReturn(Mono.empty());
        when(requestPersistencePort.findById(any(), any())).thenAnswer(invocation -> Mono.just(request));
        when(requestPersistencePort.update(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(templatePolicyPersistencePort.findActivePolicy("organization-demo", "order.confirmed")).thenReturn(Mono.just(policy));
        when(attemptPersistencePort.findByNotificationId(any(), any())).thenReturn(Flux.empty());
        when(attemptPersistencePort.create(any(), any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(attemptPersistencePort.update(any(), any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(providerCallbackPersistencePort.findByNotificationId("organization-demo", "noti-1")).thenReturn(Flux.empty());
        when(recipientResolverPort.resolve("organization-demo", "recipient-1", "EMAIL")).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(command))
                .assertNext(detail -> {
                    assertEquals("DISCARDED", detail.notification().status());
                    assertEquals(false, detail.notification().retryable());
                    assertEquals(1, detail.notification().attemptCount());
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-01T00:00:00Z"));
        when(actorContextProviderPort.currentActor()).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(new RetryNotificationCommand(
                        "organization-demo",
                        "actor-1",
                        "noti-1",
                        null)))
                .expectError(OperationNotPermittedException.class)
                .verify();
    }

    @Test
    void shouldAllowTrustedServiceActorForRelevantChangeEmission() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        EmitRelevantChangeNotificationCommand command = new EmitRelevantChangeNotificationCommand(
                "organization-demo",
                "notification-kafka-consumer",
                "evt-async-1",
                "CartCreated",
                "recipient-1",
                "EMAIL",
                "{}",
                "trace-async-1",
                "corr-async-1",
                null);

        NotificationRequest existing = pendingRequest(now);

        when(clockPort.now()).thenReturn(now);
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext(
                        "notification-kafka-consumer",
                        "organization-demo",
                        "",
                        false,
                        true)));
        when(requestPersistencePort.findByKey(any(), any())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.handle(command))
                .assertNext(result -> assertEquals(existing.notificationId().value(), result.notificationId()))
                .verifyComplete();

        verify(actorLegitimacyPort, never()).isLegitimate(any(), any());
    }

    @Test
    void shouldReplayKafkaEmissionEvenWhenAuditHashDiffers() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        EmitRelevantChangeNotificationCommand command = new EmitRelevantChangeNotificationCommand(
                "organization-demo",
                "notification-kafka-consumer",
                "evt-async-1",
                "CartCreated",
                "recipient-new",
                "EMAIL",
                "{\"organizationId\":\"organization-demo\"}",
                "trace-async-1",
                "corr-async-1",
                "kafka-emit-evt-async-1");

        NotificationRequest existing = NotificationRequest.createPending(
                NotificationId.of("noti-existing"),
                OrganizationId.of("organization-demo"),
                "evt-async-1",
                "CartCreated",
                "recipient-new",
                NotificationChannel.EMAIL,
                NotificationKey.fromEventRecipientAndChannel("evt-async-1", "recipient-new", NotificationChannel.EMAIL),
                "template-1",
                "policy-1",
                "{}",
                3,
                now,
                "trace-async-1",
                "corr-async-1",
                now);

        when(clockPort.now()).thenReturn(now);
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext(
                        "notification-kafka-consumer",
                        "organization-demo",
                        "",
                        false,
                        true)));
        when(notificationAuditPort.findByIdempotency("organization-demo", "NOTIFICATION_EMITTED", "kafka-emit-evt-async-1"))
                .thenReturn(Mono.just(new NotificationAuditEntry(
                        "audit-1",
                        "organization-demo",
                        "notification-kafka-consumer",
                        "NOTIFICATION_EMITTED",
                        "NotificationRequest",
                        "noti-existing",
                        "SUCCESS",
                        "{\"notificationId\":\"noti-existing\"}",
                        "kafka-emit-evt-async-1",
                        "legacy-different-hash",
                        now.minusSeconds(5))));
        when(requestPersistencePort.findById(any(), any())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.handle(command))
                .assertNext(result -> assertEquals("noti-existing", result.notificationId()))
                .verifyComplete();
    }

    @Test
    void shouldRecreateKafkaEmissionWhenAuditExistsButNotificationWasRemoved() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        EmitRelevantChangeNotificationCommand command = new EmitRelevantChangeNotificationCommand(
                "organization-demo",
                "notification-kafka-consumer",
                "evt-async-2",
                "CartCreated",
                "organization-demo",
                "EMAIL",
                "{\"organizationId\":\"organization-demo\"}",
                "trace-async-2",
                "corr-async-2",
                "kafka-emit-evt-async-2");

        NotificationRequest created = NotificationRequest.createPending(
                NotificationId.of("noti-recreated"),
                OrganizationId.of("organization-demo"),
                "evt-async-2",
                "CartCreated",
                "organization-demo",
                NotificationChannel.EMAIL,
                NotificationKey.fromEventRecipientAndChannel("evt-async-2", "organization-demo", NotificationChannel.EMAIL),
                "template-1",
                "policy-1",
                "{\"rendered\":true}",
                3,
                now,
                "trace-async-2",
                "corr-async-2",
                now);
        ChannelPolicy policy = new ChannelPolicy(
                "policy-1",
                "organization-demo",
                "CartCreated",
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                3,
                60,
                true);
        NotificationTemplate template = new NotificationTemplate(
                "template-1",
                "organization-demo",
                "CartCreated",
                NotificationChannel.EMAIL,
                "subject",
                "body",
                true,
                1);

        when(clockPort.now()).thenReturn(now);
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext(
                        "notification-kafka-consumer",
                        "organization-demo",
                        "",
                        false,
                        true)));
        when(notificationAuditPort.findByIdempotency("organization-demo", "NOTIFICATION_EMITTED", "kafka-emit-evt-async-2"))
                .thenReturn(Mono.just(new NotificationAuditEntry(
                        "audit-2",
                        "organization-demo",
                        "notification-kafka-consumer",
                        "NOTIFICATION_EMITTED",
                        "NotificationRequest",
                        "noti-removed",
                        "SUCCESS",
                        "{\"notificationId\":\"noti-removed\"}",
                        "kafka-emit-evt-async-2",
                        "legacy-different-hash",
                        now.minusSeconds(5))));
        when(requestPersistencePort.findById(any(), any())).thenReturn(Mono.empty());
        when(requestPersistencePort.findByKey(any(), any())).thenReturn(Mono.empty());
        when(processedEventPersistencePort.exists("evt-async-2", "notification-service")).thenReturn(Mono.just(false));
        when(templatePolicyPersistencePort.findActivePolicy("organization-demo", "CartCreated")).thenReturn(Mono.just(policy));
        when(templatePolicyPersistencePort.findActiveTemplate("organization-demo", "CartCreated", "EMAIL"))
                .thenReturn(Mono.just(template));
        when(templateRendererPort.render("subject", "body", "{\"organizationId\":\"organization-demo\"}"))
                .thenReturn(Mono.just("{\"rendered\":true}"));
        when(requestPersistencePort.create(any())).thenReturn(Mono.just(created));
        when(outboxPersistencePort.store(any(), any())).thenReturn(Mono.empty());
        when(notificationAuditPort.record(any())).thenReturn(Mono.empty());
        when(notificationSearchCachePort.evictOrganization("organization-demo")).thenReturn(Mono.empty());
        when(processedEventPersistencePort.record(any(), any(), any())).thenReturn(Mono.empty());
        when(domainEventTopicPort.topicFor(any())).thenReturn("notification.events.v1");

        StepVerifier.create(service.handle(command))
                .assertNext(result -> assertEquals("noti-recreated", result.notificationId()))
                .verifyComplete();

        verify(requestPersistencePort).create(any());
    }

    private void mockAdminActor() {
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext("actor-1", "organization-demo", "CO", true, false)));
        when(actorLegitimacyPort.isLegitimate("actor-1", "organization-demo")).thenReturn(Mono.just(Boolean.TRUE));
    }

    private NotificationRequest pendingRequest(Instant now) {
        return NotificationRequest.createPending(
                NotificationId.of("noti-1"),
                OrganizationId.of("organization-demo"),
                "evt-1",
                "order.confirmed",
                "recipient-1",
                NotificationChannel.EMAIL,
                NotificationKey.fromEventRecipientAndChannel("evt-1", "recipient-1", NotificationChannel.EMAIL),
                "template-1",
                "policy-1",
                "{}",
                3,
                now,
                "trace-1",
                "corr-1",
                now);
    }
}
