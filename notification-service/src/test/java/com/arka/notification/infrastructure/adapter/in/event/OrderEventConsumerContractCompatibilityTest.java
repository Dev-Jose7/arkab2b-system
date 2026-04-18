package com.arka.notification.infrastructure.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.notification.application.command.EmitRelevantChangeNotificationCommand;
import com.arka.notification.application.port.in.EmitRelevantChangeNotificationCommandUseCase;
import com.arka.notification.application.result.NotificationResult;
import com.arka.notification.infrastructure.adapter.out.external.OrderContextLookupHttpAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class OrderEventConsumerContractCompatibilityTest {

    @Mock
    private EmitRelevantChangeNotificationCommandUseCase emitUseCase;

    @Mock
    private OrderContextLookupHttpAdapter orderContextLookupHttpAdapter;

    @Mock
    private ObjectProvider<MeterRegistry> meterRegistryProvider;

    private OrderDomainEventKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        InboundDomainEventParser parser = new InboundDomainEventParser(new ObjectMapper());
        consumer = new OrderDomainEventKafkaConsumer(
                emitUseCase,
                parser,
                orderContextLookupHttpAdapter,
                meterRegistryProvider,
                "notification-kafka-consumer",
                "EMAIL",
                8_000,
                "OrderCreatedFromValidatedCart,CartCreated");
    }

    @Test
    void shouldConsumeOrderEventPayloadPublishedByOrderServiceContract() {
        String payload = """
                {
                  "eventId":"evt-order-1",
                  "eventType":"OrderCreatedFromValidatedCart",
                  "eventVersion":"v1",
                  "aggregateType":"Order",
                  "aggregateId":"ord-1",
                  "organizationId":"organization-1",
                  "actorId":"user-1",
                  "traceId":"trace-1",
                  "correlationId":"corr-1",
                  "occurredAt":"2026-04-16T18:00:00Z",
                  "cartId":"cart-1",
                  "orderNumber":"ORD-0001"
                }
                """;
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.events.v1", 0, 10L, "ord-1", payload);
        when(emitUseCase.handle(any())).thenReturn(Mono.just(notification("noti-1")));

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        ArgumentCaptor<EmitRelevantChangeNotificationCommand> captor =
                ArgumentCaptor.forClass(EmitRelevantChangeNotificationCommand.class);
        verify(emitUseCase).handle(captor.capture());
        verify(orderContextLookupHttpAdapter, never()).resolveByOrderId(any());

        EmitRelevantChangeNotificationCommand command = captor.getValue();
        assertThat(command.organizationId()).isEqualTo("organization-1");
        assertThat(command.recipientRef()).isEqualTo("organization-1");
        assertThat(command.sourceEventId()).isEqualTo("evt-order-1");
        assertThat(command.sourceEventType()).isEqualTo("OrderCreatedFromValidatedCart");
        assertThat(command.traceId()).isEqualTo("trace-1");
        assertThat(command.correlationId()).isEqualTo("corr-1");
        assertThat(command.idempotencyKey()).isEqualTo("kafka-emit-evt-order-1");
        assertThat(command.payloadJson()).contains("\"eventType\":\"OrderCreatedFromValidatedCart\"");
        assertThat(command.payloadJson()).contains("\"orderNumber\":\"ORD-0001\"");
    }

    @Test
    void shouldConsumeCartEventPayloadAndResolveOrganizationContextWhenMissing() {
        String payload = """
                {
                  "eventId":"evt-cart-2",
                  "eventType":"CartCreated",
                  "eventVersion":"v1",
                  "aggregateType":"Cart",
                  "aggregateId":"cart-2",
                  "traceId":"trace-2",
                  "correlationId":"corr-2",
                  "occurredAt":"2026-04-16T18:10:00Z",
                  "userId":"user-cart"
                }
                """;
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.cart.events.v1", 1, 22L, "cart-2", payload);
        when(orderContextLookupHttpAdapter.resolveByCartId("cart-2"))
                .thenReturn(Mono.just(new OrderContextLookupHttpAdapter.OrderContext("organization-cart", "actor-cart")));
        when(emitUseCase.handle(any())).thenReturn(Mono.just(notification("noti-2")));

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        ArgumentCaptor<EmitRelevantChangeNotificationCommand> captor =
                ArgumentCaptor.forClass(EmitRelevantChangeNotificationCommand.class);
        verify(emitUseCase).handle(captor.capture());

        EmitRelevantChangeNotificationCommand command = captor.getValue();
        assertThat(command.organizationId()).isEqualTo("organization-cart");
        assertThat(command.recipientRef()).isEqualTo("organization-cart");
        assertThat(command.actorId()).isEqualTo("user-cart");
        assertThat(command.sourceEventType()).isEqualTo("CartCreated");
    }

    private NotificationResult notification(String notificationId) {
        return new NotificationResult(
                notificationId,
                "organization-1",
                "evt",
                "OrderCreatedFromValidatedCart",
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
                Instant.parse("2026-04-16T18:00:00Z"),
                Instant.parse("2026-04-16T18:00:00Z"));
    }
}
