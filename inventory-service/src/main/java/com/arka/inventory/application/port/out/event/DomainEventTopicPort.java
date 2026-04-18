package com.arka.inventory.application.port.out.event;

public interface DomainEventTopicPort {

    String topicFor(String eventType);
}
