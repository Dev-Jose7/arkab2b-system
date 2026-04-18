package com.arka.order.infrastructure.adapter.out.event;

import com.arka.order.application.port.out.event.DomainEventTopicPort;
import org.springframework.stereotype.Component;

@Component
public class DefaultDomainEventTopicAdapter implements DomainEventTopicPort {

    @Override
    public String topicFor(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return "order.events.v1";
        }
        if (eventType.startsWith("Cart")) {
            return "order.cart.events.v1";
        }
        return "order.events.v1";
    }
}
