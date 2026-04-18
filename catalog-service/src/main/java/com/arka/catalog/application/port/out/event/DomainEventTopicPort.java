package com.arka.catalog.application.port.out.event;

public interface DomainEventTopicPort {

    String topicFor(String eventType);
}
