package com.arka.notification.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.notification.application.port.out.event.DomainEventPublisherPort;
import com.arka.notification.application.port.out.event.DomainEventTopicPort;
import com.arka.notification.application.port.out.persistence.OutboxRelayPort;
import com.arka.notification.application.port.out.persistence.PendingOutboxEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayPublisherTest {

    @Mock
    private OutboxRelayPort outboxRelayPort;

    @Mock
    private DomainEventTopicPort domainEventTopicPort;

    @Mock
    private DomainEventPublisherPort domainEventPublisherPort;

    @Test
    void shouldMarkEventAsPublishedWhenKafkaSendSucceeds() {
        OutboxEventRelayPublisher publisher = new OutboxEventRelayPublisher(
                outboxRelayPort,
                domainEventTopicPort,
                domainEventPublisherPort,
                3);

        PendingOutboxEvent event = new PendingOutboxEvent(
                "evt-1",
                "NotificationDispatch",
                "noti-1",
                "RelevantChangeNotificationEmitted",
                "{}",
                0);

        when(outboxRelayPort.findPending(50)).thenReturn(Flux.just(event));
        when(domainEventTopicPort.topicFor("RelevantChangeNotificationEmitted"))
                .thenReturn("notification.relevant-change-notification-emitted.v1");
        when(domainEventPublisherPort.publish("notification.relevant-change-notification-emitted.v1", "noti-1", "{}"))
                .thenReturn(Mono.empty());
        when(outboxRelayPort.markPublished(eq("evt-1"), any())).thenReturn(Mono.empty());

        StepVerifier.create(publisher.publishPending(50)).verifyComplete();

        verify(outboxRelayPort).markPublished(eq("evt-1"), any());
    }

    @Test
    void shouldMarkEventAsFailedWhenKafkaSendFails() {
        OutboxEventRelayPublisher publisher = new OutboxEventRelayPublisher(
                outboxRelayPort,
                domainEventTopicPort,
                domainEventPublisherPort,
                2);

        PendingOutboxEvent event = new PendingOutboxEvent(
                "evt-2",
                "NotificationDispatch",
                "noti-2",
                "NotificationDeliveryRecorded",
                "{}",
                1);

        when(domainEventTopicPort.topicFor("NotificationDeliveryRecorded"))
                .thenReturn("notification.notification-delivery-recorded.v1");
        when(domainEventPublisherPort.publish("notification.notification-delivery-recorded.v1", "noti-2", "{}"))
                .thenReturn(Mono.error(new RuntimeException("kafka unavailable")));
        when(outboxRelayPort.markFailed(eq("evt-2"), anyString(), any(), eq(2))).thenReturn(Mono.empty());

        StepVerifier.create(publisher.publish(event)).verifyComplete();

        verify(outboxRelayPort).markFailed(eq("evt-2"), anyString(), any(), eq(2));
    }
}
