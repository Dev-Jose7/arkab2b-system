package com.arka.inventory.infrastructure.adapter.out.event;

import com.arka.inventory.application.port.out.event.DomainEventTopicPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DomainEventKafkaMapper implements DomainEventTopicPort {

    private final String stockUpdatedTopic;
    private final String commitableAvailabilityTopic;
    private final String mutationTopic;

    public DomainEventKafkaMapper(
            @Value("${app.kafka.topics.stock-updated:inventory.stock-updated.v1}") String stockUpdatedTopic,
            @Value("${app.kafka.topics.commitable-availability-recalculated:inventory.commitable-availability-recalculated.v1}") String commitableAvailabilityTopic,
            @Value("${app.kafka.topics.inventory-mutation:inventory.mutation.v1}") String mutationTopic) {
        this.stockUpdatedTopic = stockUpdatedTopic;
        this.commitableAvailabilityTopic = commitableAvailabilityTopic;
        this.mutationTopic = mutationTopic;
    }

    @Override
    public String topicFor(String eventType) {
        return switch (eventType) {
            case "StockUpdated" -> stockUpdatedTopic;
            case "CommitableAvailabilityRecalculated" -> commitableAvailabilityTopic;
            default -> mutationTopic;
        };
    }
}
