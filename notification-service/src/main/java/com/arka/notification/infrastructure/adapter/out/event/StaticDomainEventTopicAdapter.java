package com.arka.notification.infrastructure.adapter.out.event;

import com.arka.notification.application.port.out.event.DomainEventTopicPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StaticDomainEventTopicAdapter implements DomainEventTopicPort {

    private final String notificationEmittedTopic;
    private final String notificationDeliveryTopic;
    private final String notificationMutationTopic;

    public StaticDomainEventTopicAdapter(
            @Value("${app.kafka.topics.relevant-change-notification-emitted:notification.relevant-change-notification-emitted.v1}") String notificationEmittedTopic,
            @Value("${app.kafka.topics.notification-delivery-recorded:notification.notification-delivery-recorded.v1}") String notificationDeliveryTopic,
            @Value("${app.kafka.topics.notification-mutation:notification.mutation.v1}") String notificationMutationTopic) {
        this.notificationEmittedTopic = notificationEmittedTopic;
        this.notificationDeliveryTopic = notificationDeliveryTopic;
        this.notificationMutationTopic = notificationMutationTopic;
    }

    @Override
    public String topicFor(String eventType) {
        return switch (eventType) {
            case "RelevantChangeNotificationEmitted" -> notificationEmittedTopic;
            case "NotificationDeliveryRecorded" -> notificationDeliveryTopic;
            default -> notificationMutationTopic;
        };
    }
}
