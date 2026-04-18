package com.arka.directory.application.port.out.event;

public interface DomainEventTopicPort {

    String topicFor(String eventType);
}
