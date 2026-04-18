package com.arka.directory.infrastructure.adapter.out.event;

import com.arka.directory.application.port.out.event.DomainEventTopicPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DomainEventKafkaMapper implements DomainEventTopicPort {

    private final String regionalPolicyConfiguredTopic;
    private final String regionalPolicyAppliedTopic;
    private final String directoryEntityMutatedTopic;

    public DomainEventKafkaMapper(
            @Value("${app.kafka.topics.regional-policy-configured:directory.regional-policy-configured.v1}") String regionalPolicyConfiguredTopic,
            @Value("${app.kafka.topics.regional-policy-applied:directory.regional-policy-applied.v1}") String regionalPolicyAppliedTopic,
            @Value("${app.kafka.topics.directory-entity-mutated:directory.entity-mutated.v1}") String directoryEntityMutatedTopic) {
        this.regionalPolicyConfiguredTopic = regionalPolicyConfiguredTopic;
        this.regionalPolicyAppliedTopic = regionalPolicyAppliedTopic;
        this.directoryEntityMutatedTopic = directoryEntityMutatedTopic;
    }

    @Override
    public String topicFor(String eventType) {
        return switch (eventType) {
            case "RegionalPolicyConfigured" -> regionalPolicyConfiguredTopic;
            case "RegionalPolicyAppliedInOperation" -> regionalPolicyAppliedTopic;
            case "OrganizationRegistered", "OrganizationStatusChanged" -> directoryEntityMutatedTopic;
            default -> directoryEntityMutatedTopic;
        };
    }
}
