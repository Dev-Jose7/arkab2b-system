package com.arka.order.application.service;

import com.arka.order.application.port.out.event.DomainEventPublisherPort;
import com.arka.order.application.port.out.event.DomainEventTopicPort;
import com.arka.order.application.port.out.persistence.OutboxRelayPort;
import com.arka.order.application.port.out.persistence.PendingOutboxEvent;
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
        return outboxRelayPort.findPending(batchSize)
                .concatMap(this::publish)
                .then();
    }

    public Mono<Void> publish(PendingOutboxEvent event) {
        return Mono.defer(() -> {
                    String topic = domainEventTopicPort.topicFor(event.eventType());
                    return domainEventPublisherPort
                            .publish(topic, event.aggregateId(), event.payload())
                            .then(outboxRelayPort.markPublished(event.eventId(), Instant.now()));
                })
                .onErrorResume(error -> {
                    String message = truncateError(error);
                    log.error(
                            "Outbox publish failed eventId={} eventType={} aggregateId={} retryCount={} error={}",
                            event.eventId(),
                            event.eventType(),
                            event.aggregateId(),
                            event.retryCount(),
                            message,
                            error);
                    return outboxRelayPort.markFailed(event.eventId(), message, Instant.now(), maxRetries)
                            .onErrorResume(persistenceError -> Mono.empty());
                });
    }

    private String truncateError(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        String trimmed = message.trim();
        return trimmed.length() <= 2000 ? trimmed : trimmed.substring(0, 2000);
    }
}
