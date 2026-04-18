package com.arka.reporting.infrastructure.adapter.in.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.reporting.application.command.ApplyAnalyticFactCommand;
import com.arka.reporting.application.command.RegisterAnalyticFactCommand;
import com.arka.reporting.application.command.UpdateConsumerCheckpointCommand;
import com.arka.reporting.application.port.in.ApplyAnalyticFactCommandUseCase;
import com.arka.reporting.application.port.in.RegisterAnalyticFactCommandUseCase;
import com.arka.reporting.application.port.in.UpdateConsumerCheckpointCommandUseCase;
import com.arka.reporting.application.result.AnalyticFactResult;
import com.arka.reporting.infrastructure.adapter.in.event.InboundDomainEventParser.ParsedInboundDomainEvent;
import com.arka.reporting.infrastructure.adapter.out.external.OrderTenantLookupHttpAdapter;
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
class UpstreamDomainEventKafkaConsumerTest {

    @Mock
    private RegisterAnalyticFactCommandUseCase registerUseCase;

    @Mock
    private ApplyAnalyticFactCommandUseCase applyUseCase;

    @Mock
    private UpdateConsumerCheckpointCommandUseCase checkpointUseCase;

    @Mock
    private InboundDomainEventParser parser;

    @Mock
    private OrderTenantLookupHttpAdapter orderTenantLookupHttpAdapter;

    private UpstreamDomainEventKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UpstreamDomainEventKafkaConsumer(
                registerUseCase,
                applyUseCase,
                checkpointUseCase,
                parser,
                orderTenantLookupHttpAdapter,
                "reporting-kafka-consumer",
                "reporting-service",
                7_000);
    }

    @Test
    void shouldRouteEventToRegisterApplyAndCheckpoint() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.events.v1", 2, 11L, "k", "{\"eventType\":\"OrderCreatedFromValidatedCart\"}");
        ParsedInboundDomainEvent event = parsedEvent(
                "evt-11",
                "OrderCreatedFromValidatedCart",
                "Order",
                "ord-11",
                "tenant-11",
                null);

        when(parser.parse(record.value())).thenReturn(event);
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-11", "evt-11", "OrderCreatedFromValidatedCart")));
        when(applyUseCase.handle(any())).thenReturn(Mono.just(fact("fact-11", "evt-11", "OrderCreatedFromValidatedCart")));
        when(checkpointUseCase.handle(any())).thenReturn(Mono.empty());

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        ArgumentCaptor<RegisterAnalyticFactCommand> registerCaptor = ArgumentCaptor.forClass(RegisterAnalyticFactCommand.class);
        ArgumentCaptor<ApplyAnalyticFactCommand> applyCaptor = ArgumentCaptor.forClass(ApplyAnalyticFactCommand.class);
        ArgumentCaptor<UpdateConsumerCheckpointCommand> checkpointCaptor =
                ArgumentCaptor.forClass(UpdateConsumerCheckpointCommand.class);

        verify(registerUseCase).handle(registerCaptor.capture());
        verify(applyUseCase).handle(applyCaptor.capture());
        verify(checkpointUseCase).handle(checkpointCaptor.capture());

        assertEquals("tenant-11", registerCaptor.getValue().tenantId());
        assertEquals("SALES", registerCaptor.getValue().factType());
        assertEquals("fact-11", applyCaptor.getValue().factId());
        assertEquals(record.topic(), checkpointCaptor.getValue().topic());
        assertEquals(record.partition(), checkpointCaptor.getValue().partition());
        assertEquals(record.offset(), checkpointCaptor.getValue().currentOffset());
    }

    @Test
    void shouldResolveTenantFromOrderLookupWhenMissingInEvent() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.events.v1", 0, 22L, "k", "{\"eventType\":\"OrderOperationalStatusUpdated\"}");
        when(parser.parse(record.value()))
                .thenReturn(parsedEvent("evt-22", "OrderOperationalStatusUpdated", "Order", "ord-22", null, null));
        when(orderTenantLookupHttpAdapter.resolveTenantByOrderId("ord-22")).thenReturn(Mono.just("tenant-resolved"));
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-22", "evt-22", "OrderOperationalStatusUpdated")));
        when(applyUseCase.handle(any())).thenReturn(Mono.just(fact("fact-22", "evt-22", "OrderOperationalStatusUpdated")));
        when(checkpointUseCase.handle(any())).thenReturn(Mono.empty());

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        verify(orderTenantLookupHttpAdapter).resolveTenantByOrderId("ord-22");
    }

    @Test
    void shouldResolveTenantFromCartLookupForCartEvents() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.cart.events.v1", 0, 23L, "k", "{\"eventType\":\"CartCreated\"}");
        when(parser.parse(record.value())).thenReturn(parsedEvent("evt-23", "CartCreated", "Cart", "cart-23", null, null));
        when(orderTenantLookupHttpAdapter.resolveTenantByCartId("cart-23")).thenReturn(Mono.just("tenant-cart"));
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-23", "evt-23", "CartCreated")));
        when(applyUseCase.handle(any())).thenReturn(Mono.just(fact("fact-23", "evt-23", "CartCreated")));
        when(checkpointUseCase.handle(any())).thenReturn(Mono.empty());

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        verify(orderTenantLookupHttpAdapter).resolveTenantByCartId("cart-23");
        verify(orderTenantLookupHttpAdapter, never()).resolveTenantByOrderId(any());
    }

    @Test
    void shouldSkipWhenTenantCannotBeResolved() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("catalog.offer-published.v1", 0, 99L, "k", "{\"eventType\":\"CatalogOfferPublished\"}");
        when(parser.parse(record.value()))
                .thenReturn(parsedEvent("evt-99", "CatalogOfferPublished", "CatalogOffer", "offer-99", null, null));

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        verify(registerUseCase, never()).handle(any());
    }

    @Test
    void shouldFailWhenEventTypeMissing() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("any.topic", 0, 1L, "k", "{}");
        when(parser.parse(record.value())).thenReturn(parsedEvent("evt-x", "", "Order", "ord-x", "tenant-1", null));

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
            String tenantId,
            String organizationId) {
        return new ParsedInboundDomainEvent(
                eventId,
                eventType,
                aggregateType,
                aggregateId,
                tenantId,
                organizationId,
                "actor-x",
                "trace-x",
                "corr-x",
                Instant.parse("2026-04-14T10:00:00Z"),
                "{\"payload\":true}",
                "{\"payload\":true}");
    }

    private AnalyticFactResult fact(String factId, String sourceEventId, String eventType) {
        return new AnalyticFactResult(
                factId,
                "tenant",
                sourceEventId,
                eventType,
                "SALES",
                "CAPTURED",
                "{}",
                null,
                null,
                Instant.parse("2026-04-14T10:00:00Z"),
                Instant.parse("2026-04-14T10:00:00Z"),
                Instant.parse("2026-04-14T10:00:00Z"));
    }
}
