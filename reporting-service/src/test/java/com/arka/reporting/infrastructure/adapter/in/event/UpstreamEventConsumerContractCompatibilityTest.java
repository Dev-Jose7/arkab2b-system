package com.arka.reporting.infrastructure.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.reporting.application.command.RegisterAnalyticFactCommand;
import com.arka.reporting.application.port.in.ApplyAnalyticFactCommandUseCase;
import com.arka.reporting.application.port.in.RegisterAnalyticFactCommandUseCase;
import com.arka.reporting.application.port.in.UpdateConsumerCheckpointCommandUseCase;
import com.arka.reporting.application.result.AnalyticFactResult;
import com.arka.reporting.infrastructure.adapter.out.external.OrderTenantLookupHttpAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UpstreamEventConsumerContractCompatibilityTest {

    @Mock
    private RegisterAnalyticFactCommandUseCase registerUseCase;

    @Mock
    private ApplyAnalyticFactCommandUseCase applyUseCase;

    @Mock
    private UpdateConsumerCheckpointCommandUseCase checkpointUseCase;

    @Mock
    private OrderTenantLookupHttpAdapter orderTenantLookupHttpAdapter;

    private UpstreamDomainEventKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        InboundDomainEventParser parser = new InboundDomainEventParser(new ObjectMapper());
        consumer = new UpstreamDomainEventKafkaConsumer(
                registerUseCase,
                applyUseCase,
                checkpointUseCase,
                parser,
                orderTenantLookupHttpAdapter,
                "reporting-kafka-consumer",
                "reporting-service",
                8_000);
        when(applyUseCase.handle(any())).thenReturn(Mono.just(fact("fact-applied", "evt", "Event")));
        when(checkpointUseCase.handle(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAcceptOrderEventContractAndMapToSalesFact() {
        String payload = """
                {
                  "eventId":"evt-order-101",
                  "eventType":"OrderCreatedFromValidatedCart",
                  "eventVersion":"v1",
                  "aggregateType":"Order",
                  "aggregateId":"ord-101",
                  "tenantId":"tenant-order",
                  "traceId":"trace-order",
                  "correlationId":"corr-order",
                  "occurredAt":"2026-04-16T19:00:00Z"
                }
                """;
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-order", "evt-order-101", "OrderCreatedFromValidatedCart")));

        StepVerifier.create(consumer.consume(new ConsumerRecord<>("order.events.v1", 0, 1L, "ord-101", payload)))
                .verifyComplete();

        ArgumentCaptor<RegisterAnalyticFactCommand> captor = ArgumentCaptor.forClass(RegisterAnalyticFactCommand.class);
        verify(registerUseCase).handle(captor.capture());
        assertThat(captor.getValue().tenantId()).isEqualTo("tenant-order");
        assertThat(captor.getValue().factType()).isEqualTo("SALES");
        assertThat(captor.getValue().sourceEventType()).isEqualTo("OrderCreatedFromValidatedCart");
    }

    @Test
    void shouldAcceptInventoryEventContractAndMapToReplenishmentFact() {
        String payload = """
                {
                  "eventId":"evt-inv-1",
                  "eventType":"InventoryStockUpdated",
                  "eventVersion":"v1",
                  "aggregateType":"InventoryBalance",
                  "aggregateId":"inv-1",
                  "tenantId":"tenant-inv",
                  "traceId":"trace-inv",
                  "correlationId":"corr-inv",
                  "occurredAt":"2026-04-16T19:10:00Z"
                }
                """;
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-inv", "evt-inv-1", "InventoryStockUpdated")));

        StepVerifier.create(consumer.consume(new ConsumerRecord<>("inventory.stock-updated.v1", 0, 2L, "inv-1", payload)))
                .verifyComplete();

        ArgumentCaptor<RegisterAnalyticFactCommand> captor = ArgumentCaptor.forClass(RegisterAnalyticFactCommand.class);
        verify(registerUseCase).handle(captor.capture());
        assertThat(captor.getValue().tenantId()).isEqualTo("tenant-inv");
        assertThat(captor.getValue().factType()).isEqualTo("REPLENISHMENT");
    }

    @Test
    void shouldAcceptNotificationEventContractAndMapToNotificationFact() {
        String payload = """
                {
                  "eventId":"evt-noti-1",
                  "eventType":"NotificationDeliveryRecorded",
                  "eventVersion":"v1",
                  "aggregateType":"NotificationDispatch",
                  "aggregateId":"noti-1",
                  "tenantId":"tenant-noti",
                  "traceId":"trace-noti",
                  "correlationId":"corr-noti",
                  "occurredAt":"2026-04-16T19:20:00Z"
                }
                """;
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-noti", "evt-noti-1", "NotificationDeliveryRecorded")));

        StepVerifier.create(consumer.consume(new ConsumerRecord<>("notification.notification-delivery-recorded.v1", 1, 3L, "noti-1", payload)))
                .verifyComplete();

        ArgumentCaptor<RegisterAnalyticFactCommand> captor = ArgumentCaptor.forClass(RegisterAnalyticFactCommand.class);
        verify(registerUseCase).handle(captor.capture());
        assertThat(captor.getValue().tenantId()).isEqualTo("tenant-noti");
        assertThat(captor.getValue().factType()).isEqualTo("NOTIFICATION");
    }

    @Test
    void shouldAcceptCatalogEventContractAndMapToGenericFact() {
        String payload = """
                {
                  "eventId":"evt-cat-1",
                  "eventType":"CatalogOfferPublished",
                  "eventVersion":"v1",
                  "aggregateType":"CatalogOffer",
                  "aggregateId":"offer-1",
                  "tenantId":"tenant-cat",
                  "traceId":"trace-cat",
                  "correlationId":"corr-cat",
                  "occurredAt":"2026-04-16T19:25:00Z"
                }
                """;
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-cat", "evt-cat-1", "CatalogOfferPublished")));

        StepVerifier.create(consumer.consume(new ConsumerRecord<>("catalog.offer-published.v1", 1, 8L, "offer-1", payload)))
                .verifyComplete();

        ArgumentCaptor<RegisterAnalyticFactCommand> captor = ArgumentCaptor.forClass(RegisterAnalyticFactCommand.class);
        verify(registerUseCase).handle(captor.capture());
        assertThat(captor.getValue().tenantId()).isEqualTo("tenant-cat");
        assertThat(captor.getValue().factType()).isEqualTo("GENERIC");
    }

    @Test
    void shouldAcceptDirectoryEventContractUsingAggregateAsTenantWhenNeeded() {
        String payload = """
                {
                  "eventId":"evt-dir-1",
                  "eventType":"OrganizationStatusChanged",
                  "eventVersion":"v1",
                  "aggregateType":"Organization",
                  "aggregateId":"tenant-dir",
                  "traceId":"trace-dir",
                  "correlationId":"corr-dir",
                  "occurredAt":"2026-04-16T19:30:00Z"
                }
                """;
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-dir", "evt-dir-1", "OrganizationStatusChanged")));

        StepVerifier.create(consumer.consume(new ConsumerRecord<>("directory.entity-mutated.v1", 2, 4L, "tenant-dir", payload)))
                .verifyComplete();

        ArgumentCaptor<RegisterAnalyticFactCommand> captor = ArgumentCaptor.forClass(RegisterAnalyticFactCommand.class);
        verify(registerUseCase).handle(captor.capture());
        assertThat(captor.getValue().tenantId()).isEqualTo("tenant-dir");
        assertThat(captor.getValue().factType()).isEqualTo("OPERATIONS");
    }

    private AnalyticFactResult fact(String factId, String sourceEventId, String sourceEventType) {
        return new AnalyticFactResult(
                factId,
                "tenant",
                sourceEventId,
                sourceEventType,
                "GENERIC",
                "CAPTURED",
                "{}",
                null,
                null,
                Instant.parse("2026-04-16T19:00:00Z"),
                Instant.parse("2026-04-16T19:00:00Z"),
                Instant.parse("2026-04-16T19:00:00Z"));
    }
}
