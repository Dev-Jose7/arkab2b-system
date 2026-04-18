package com.arka.notification.infrastructure.adapter.in.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.notification.application.port.in.EmitRelevantChangeNotificationCommandUseCase;
import com.arka.notification.application.result.NotificationResult;
import com.arka.notification.infrastructure.adapter.in.event.InboundDomainEventParser.ParsedInboundDomainEvent;
import com.arka.notification.infrastructure.adapter.out.external.OrderContextLookupHttpAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrderDomainEventKafkaConsumerTest {

    @Mock
    private EmitRelevantChangeNotificationCommandUseCase emitUseCase;

    @Mock
    private InboundDomainEventParser parser;

    @Mock
    private OrderContextLookupHttpAdapter orderContextLookupHttpAdapter;

    @Mock
    private ObjectProvider<MeterRegistry> meterRegistryProvider;

    private OrderDomainEventKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderDomainEventKafkaConsumer(
                emitUseCase,
                parser,
                orderContextLookupHttpAdapter,
                meterRegistryProvider,
                "notification-kafka-consumer",
                "EMAIL",
                5_000,
                "OrderCreatedFromValidatedCart,OrderOperationalStatusUpdated,CartCreated");
    }

    @Test
    void shouldRouteSupportedEventToEmitUseCase() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.events.v1", 0, 1L, "k", "{\"eventType\":\"OrderCreatedFromValidatedCart\"}");
        ParsedInboundDomainEvent event = parsedEvent(
                "evt-1",
                "OrderCreatedFromValidatedCart",
                "Order",
                "ord-1",
                "organization-1",
                "actor-1");

        when(parser.parse(record.value())).thenReturn(event);
        when(emitUseCase.handle(any())).thenReturn(Mono.just(notificationResult("noti-1")));

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        ArgumentCaptor<com.arka.notification.application.command.EmitRelevantChangeNotificationCommand> commandCaptor =
                ArgumentCaptor.forClass(com.arka.notification.application.command.EmitRelevantChangeNotificationCommand.class);
        verify(emitUseCase).handle(commandCaptor.capture());
        assertEquals("organization-1", commandCaptor.getValue().organizationId());
        assertEquals("organization-1", commandCaptor.getValue().recipientRef());
        assertEquals("OrderCreatedFromValidatedCart", commandCaptor.getValue().sourceEventType());
        assertEquals("kafka-emit-evt-1", commandCaptor.getValue().idempotencyKey());
    }

    @Test
    void shouldSkipUnsupportedEventType() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.events.v1", 0, 2L, "k", "{\"eventType\":\"OrderCancelled\"}");
        when(parser.parse(record.value()))
                .thenReturn(parsedEvent("evt-2", "OrderCancelled", "Order", "ord-2", "organization-1", "actor-1"));

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        verify(emitUseCase, never()).handle(any());
    }

    @Test
    void shouldResolveContextFromOrderServiceWhenOrganizationOrRecipientMissing() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.events.v1", 0, 3L, "k", "{\"eventType\":\"OrderOperationalStatusUpdated\"}");
        when(parser.parse(record.value()))
                .thenReturn(parsedEvent("evt-3", "OrderOperationalStatusUpdated", "Order", "ord-3", null, null));
        when(orderContextLookupHttpAdapter.resolveByOrderId("ord-3"))
                .thenReturn(Mono.just(new OrderContextLookupHttpAdapter.OrderContext("organization-x", "service-actor")));
        when(emitUseCase.handle(any())).thenReturn(Mono.just(notificationResult("noti-3")));

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        ArgumentCaptor<com.arka.notification.application.command.EmitRelevantChangeNotificationCommand> commandCaptor =
                ArgumentCaptor.forClass(com.arka.notification.application.command.EmitRelevantChangeNotificationCommand.class);
        verify(emitUseCase).handle(commandCaptor.capture());
        assertEquals("organization-x", commandCaptor.getValue().organizationId());
        assertEquals("organization-x", commandCaptor.getValue().recipientRef());
        assertEquals("service-actor", commandCaptor.getValue().actorId());
    }

    @Test
    void shouldResolveCartContextUsingCartLookup() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.cart.events.v1", 0, 4L, "k", "{\"eventType\":\"CartCreated\"}");
        when(parser.parse(record.value()))
                .thenReturn(parsedEvent("evt-4", "CartCreated", "Cart", "cart-44", null, null));
        when(orderContextLookupHttpAdapter.resolveByCartId("cart-44"))
                .thenReturn(Mono.just(new OrderContextLookupHttpAdapter.OrderContext("organization-c", "actor-c")));
        when(emitUseCase.handle(any())).thenReturn(Mono.just(notificationResult("noti-4")));

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        verify(orderContextLookupHttpAdapter).resolveByCartId("cart-44");
        verify(orderContextLookupHttpAdapter, never()).resolveByOrderId(any());
    }

    @Test
    void shouldSkipWhenNoOrganizationOrRecipientCanBeResolved() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.events.v1", 0, 5L, "k", "{\"eventType\":\"OrderOperationalStatusUpdated\"}");
        when(parser.parse(record.value()))
                .thenReturn(parsedEvent("evt-5", "OrderOperationalStatusUpdated", "Order", "ord-5", null, null));
        when(orderContextLookupHttpAdapter.resolveByOrderId("ord-5")).thenReturn(Mono.empty());

        StepVerifier.create(consumer.consume(record)).verifyComplete();
        verify(emitUseCase, never()).handle(any());
    }

    @Test
    void shouldFailWhenEventTypeIsMissing() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.events.v1", 0, 6L, "k", "{}");
        when(parser.parse(record.value()))
                .thenReturn(parsedEvent("evt-6", "", "Order", "ord-6", "organization-1", "actor-1"));

        StepVerifier.create(consumer.consume(record))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains("missing eventType"))
                .verify();
    }

    private ParsedInboundDomainEvent parsedEvent(
            String eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            String organizationId,
            String actorId) {
        return new ParsedInboundDomainEvent(
                eventId,
                eventType,
                aggregateType,
                aggregateId,
                organizationId,
                actorId,
                "trace-1",
                "corr-1",
                Instant.parse("2026-04-14T10:00:00Z"),
                "{\"event\":\"payload\"}",
                "{\"event\":\"payload\"}");
    }

    private NotificationResult notificationResult(String notificationId) {
        return new NotificationResult(
                notificationId,
                "organization-1",
                "evt",
                "type",
                "org-1",
                "EMAIL",
                "tpl-1",
                "policy-1",
                "PENDING",
                true,
                null,
                3,
                0,
                "key-1",
                "trace",
                "corr",
                0L,
                Instant.parse("2026-04-14T10:00:00Z"),
                Instant.parse("2026-04-14T10:00:00Z"));
    }
}
