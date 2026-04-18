package com.arka.inventory.application.service;

import com.arka.inventory.application.port.out.event.DomainEventPublisherPort;
import com.arka.inventory.application.port.out.event.DomainEventTopicPort;
import com.arka.inventory.application.port.out.persistence.OutboxRelayPort;
import com.arka.inventory.application.port.out.persistence.PendingOutboxEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
    void publishPendingMarksEventsAsPublished() {
        OutboxEventRelayPublisher publisher = new OutboxEventRelayPublisher(
                outboxRelayPort,
                domainEventTopicPort,
                domainEventPublisherPort,
                3);

        PendingOutboxEvent event = new PendingOutboxEvent(
                "evt-1",
                "InventoryBalance",
                "stock-1",
                "StockUpdated",
                "{}",
                0);

        when(outboxRelayPort.findPending(50)).thenReturn(Flux.just(event));
        when(domainEventTopicPort.topicFor("StockUpdated")).thenReturn("inventory.stock-updated.v1");
        when(domainEventPublisherPort.publish("inventory.stock-updated.v1", "stock-1", "{}")).thenReturn(Mono.empty());
        when(outboxRelayPort.markPublished(org.mockito.ArgumentMatchers.eq("evt-1"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(publisher.publishPending(50)).verifyComplete();

        verify(outboxRelayPort).markPublished(org.mockito.ArgumentMatchers.eq("evt-1"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void publishFailureMarksEventAsFailed() {
        OutboxEventRelayPublisher publisher = new OutboxEventRelayPublisher(
                outboxRelayPort,
                domainEventTopicPort,
                domainEventPublisherPort,
                2);

        PendingOutboxEvent event = new PendingOutboxEvent(
                "evt-2",
                "InventoryBalance",
                "stock-2",
                "StockUpdated",
                "{}",
                1);

        when(domainEventTopicPort.topicFor("StockUpdated")).thenReturn("inventory.stock-updated.v1");
        when(domainEventPublisherPort.publish("inventory.stock-updated.v1", "stock-2", "{}"))
                .thenReturn(Mono.error(new RuntimeException("kafka unavailable")));
        when(outboxRelayPort.markFailed(
                org.mockito.ArgumentMatchers.eq("evt-2"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(2)))
                .thenReturn(Mono.empty());

        StepVerifier.create(publisher.publish(event)).verifyComplete();

        verify(outboxRelayPort).markFailed(
                org.mockito.ArgumentMatchers.eq("evt-2"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(2));
    }
}
