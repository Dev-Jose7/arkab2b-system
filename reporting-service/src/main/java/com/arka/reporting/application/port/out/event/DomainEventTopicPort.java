package com.arka.reporting.application.port.out.event;

public interface DomainEventTopicPort {

    String topicFor(String eventType);
}
