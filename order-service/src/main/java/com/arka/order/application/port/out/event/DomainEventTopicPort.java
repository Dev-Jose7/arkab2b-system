package com.arka.order.application.port.out.event;

public interface DomainEventTopicPort {

    String topicFor(String eventType);
}
