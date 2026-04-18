package com.arka.notification.application.port.out.event;

public interface DomainEventTopicPort {

    String topicFor(String eventType);
}
