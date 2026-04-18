package com.arka.directory.application.service;

import com.arka.directory.application.port.out.event.DomainEventPublisherPort;
import com.arka.directory.application.port.out.event.DomainEventTopicPort;
import com.arka.directory.application.port.out.persistence.OutboxRelayPort;
import com.arka.directory.application.port.out.persistence.PendingOutboxEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayPublisherTest {

    @Mock
    private OutboxRelayPort outboxRelayPort;
    @Mock
    private DomainEventTopicPort domainEventTopicPort;
    @Mock
    private DomainEventPublisherPort domainEventPublisherPort;

    @Test
    void publishPendingMarksEventAsPublished() {
        PendingOutboxEvent event = new PendingOutboxEvent(
                "event-1",
                "CountryPolicy",
                "org-1",
                "RegionalPolicyConfigured",
                "{\"type\":\"RegionalPolicyConfigured\"}",
                0);

        when(outboxRelayPort.findPending(50)).thenReturn(Flux.just(event));
        when(domainEventTopicPort.topicFor("RegionalPolicyConfigured")).thenReturn("directory.regional-policy-configured.v1");
        when(domainEventPublisherPort.publish(any(), any(), any())).thenReturn(Mono.empty());
        when(outboxRelayPort.markPublished(eq("event-1"), any(Instant.class))).thenReturn(Mono.empty());

        OutboxEventRelayPublisher relayPublisher = new OutboxEventRelayPublisher(
                outboxRelayPort,
                domainEventTopicPort,
                domainEventPublisherPort,
                3);

        StepVerifier.create(relayPublisher.publishPending(50))
                .verifyComplete();

        verify(outboxRelayPort).markPublished(eq("event-1"), any(Instant.class));
    }

    @Test
    void publishFailureMarksEventAsFailed() {
        PendingOutboxEvent event = new PendingOutboxEvent(
                "event-1",
                "CountryPolicy",
                "org-1",
                "RegionalPolicyConfigured",
                "{\"type\":\"RegionalPolicyConfigured\"}",
                1);

        when(outboxRelayPort.findPending(anyInt())).thenReturn(Flux.just(event));
        when(domainEventTopicPort.topicFor(any())).thenReturn("directory.regional-policy-configured.v1");
        when(domainEventPublisherPort.publish(any(), any(), any())).thenReturn(Mono.error(new RuntimeException("broker down")));
        when(outboxRelayPort.markFailed(eq("event-1"), any(), any(Instant.class), eq(3))).thenReturn(Mono.empty());

        OutboxEventRelayPublisher relayPublisher = new OutboxEventRelayPublisher(
                outboxRelayPort,
                domainEventTopicPort,
                domainEventPublisherPort,
                3);

        StepVerifier.create(relayPublisher.publishPending(20))
                .verifyComplete();

        verify(outboxRelayPort).markFailed(eq("event-1"), any(), any(Instant.class), eq(3));
    }
}
