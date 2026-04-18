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
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.entity.ProviderCallback;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.notificationdispatch.enumtype.ProviderCallbackStatus;
import com.arka.notification.domain.notificationdispatch.exception.DiscardedNotificationCannotDispatchException;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.TenantId;
import com.arka.notification.domain.shared.exception.OperationNotPermittedException;
import java.time.Instant;
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
                "tenant-demo",
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
                "tenant-demo",
                "actor-1",
                "noti-1",
                null);

        NotificationRequest discarded = pendingRequest(now);
        discarded.discard(now.plusSeconds(1));

        ChannelPolicy policy = new ChannelPolicy(
                "policy-1",
                "tenant-demo",
                "order.confirmed",
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                3,
                60,
                true);

        mockAdminActor();
        when(clockPort.now()).thenReturn(now.plusSeconds(2));
        when(requestPersistencePort.findById(any(), any())).thenReturn(Mono.just(discarded));
        when(templatePolicyPersistencePort.findActivePolicy("tenant-demo", "order.confirmed")).thenReturn(Mono.just(policy));

        StepVerifier.create(service.handle(command))
                .expectError(DiscardedNotificationCannotDispatchException.class)
                .verify();

        verify(attemptPersistencePort, never()).create(any(), any());
    }

    @Test
    void shouldTreatDuplicatedProviderCallbackAsIdempotentNoop() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        ProcessProviderCallbackCommand command = new ProcessProviderCallbackCommand(
                "tenant-demo",
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
                "tenant-demo",
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
        when(providerCallbackPersistencePort.findByNotificationId("tenant-demo", "noti-1"))
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
                "tenant-demo",
                "actor-1",
                "noti-1",
                null);

        NotificationRequest request = pendingRequest(now);
        ChannelPolicy policy = new ChannelPolicy(
                "policy-1",
                "tenant-demo",
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
        when(notificationSearchCachePort.evictTenant(any())).thenReturn(Mono.empty());

        when(requestPersistencePort.findById(any(), any())).thenReturn(Mono.just(request));
        when(requestPersistencePort.update(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(templatePolicyPersistencePort.findActivePolicy("tenant-demo", "order.confirmed")).thenReturn(Mono.just(policy));
        when(attemptPersistencePort.create(any(), any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(attemptPersistencePort.update(any(), any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(attemptPersistencePort.findByNotificationId(any(), any())).thenReturn(Flux.empty());
        when(providerCallbackPersistencePort.findByNotificationId("tenant-demo", "noti-1")).thenReturn(Flux.empty());
        when(recipientResolverPort.resolve("tenant-demo", "recipient-1", "EMAIL"))
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
    void shouldRejectWhenActorContextIsMissing() {
        when(clockPort.now()).thenReturn(Instant.parse("2026-04-01T00:00:00Z"));
        when(actorContextProviderPort.currentActor()).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(new RetryNotificationCommand(
                        "tenant-demo",
                        "actor-1",
                        "noti-1",
                        null)))
                .expectError(OperationNotPermittedException.class)
                .verify();
    }

    private void mockAdminActor() {
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext("actor-1", "tenant-demo", "CO", true, false)));
        when(actorLegitimacyPort.isLegitimate("actor-1", "tenant-demo")).thenReturn(Mono.just(Boolean.TRUE));
    }

    private NotificationRequest pendingRequest(Instant now) {
        return NotificationRequest.createPending(
                NotificationId.of("noti-1"),
                TenantId.of("tenant-demo"),
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
