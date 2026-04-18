package com.arka.catalog.application.service;

import com.arka.catalog.application.port.out.event.DomainEventPublisherPort;
import com.arka.catalog.application.port.out.event.DomainEventTopicPort;
import com.arka.catalog.application.port.out.persistence.OutboxRelayPort;
import com.arka.catalog.application.port.out.persistence.PendingOutboxEvent;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OutboxEventRelayPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventRelayPublisher.class);

    private final OutboxRelayPort outboxRelayPort;
    private final DomainEventTopicPort domainEventTopicPort;
    private final DomainEventPublisherPort domainEventPublisherPort;
    private final int maxRetries;

    public OutboxEventRelayPublisher(
            OutboxRelayPort outboxRelayPort,
            DomainEventTopicPort domainEventTopicPort,
            DomainEventPublisherPort domainEventPublisherPort,
            @Value("${app.outbox.relay.max-retries:3}") int maxRetries) {
        this.outboxRelayPort = outboxRelayPort;
        this.domainEventTopicPort = domainEventTopicPort;
        this.domainEventPublisherPort = domainEventPublisherPort;
        this.maxRetries = Math.max(1, maxRetries);
    }

    public Mono<Void> publishPending(int batchSize) {
        return outboxRelayPort.findPending(batchSize).concatMap(this::publish).then();
    }

    private Mono<Void> publish(PendingOutboxEvent pendingOutboxEvent) {
        return Mono.defer(() -> {
                    String topic = domainEventTopicPort.topicFor(pendingOutboxEvent.eventType());
                    return domainEventPublisherPort
                            .publish(topic, pendingOutboxEvent.aggregateId(), pendingOutboxEvent.payload())
                            .then(outboxRelayPort.markPublished(pendingOutboxEvent.eventId(), Instant.now()));
                })
                .onErrorResume(throwable -> {
                    String error = throwable.getMessage() == null ? throwable.getClass().getSimpleName() : throwable.getMessage();
                    log.error(
                            "Outbox publish failed eventId={} eventType={} error={}",
                            pendingOutboxEvent.eventId(),
                            pendingOutboxEvent.eventType(),
                            error,
                            throwable);
                    return outboxRelayPort.markFailed(
                            pendingOutboxEvent.eventId(),
                            error,
                            Instant.now(),
                            maxRetries);
                });
    }
}
