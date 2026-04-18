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
import com.arka.reporting.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.reporting.infrastructure.adapter.out.external.OrderOrganizationLookupHttpAdapter;
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
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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
    private OrderOrganizationLookupHttpAdapter orderOrganizationLookupHttpAdapter;

    @Mock
    private ObjectProvider<MeterRegistry> meterRegistryProvider;

    private UpstreamDomainEventKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UpstreamDomainEventKafkaConsumer(
                registerUseCase,
                applyUseCase,
                checkpointUseCase,
                parser,
                orderOrganizationLookupHttpAdapter,
                meterRegistryProvider,
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
                "organization-11",
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

        assertEquals("organization-11", registerCaptor.getValue().organizationId());
        assertEquals("SALES", registerCaptor.getValue().factType());
        assertEquals("fact-11", applyCaptor.getValue().factId());
        assertEquals(record.topic(), checkpointCaptor.getValue().topic());
        assertEquals(record.partition(), checkpointCaptor.getValue().partition());
        assertEquals(record.offset(), checkpointCaptor.getValue().currentOffset());
    }

    @Test
    void shouldPropagateTechnicalAuthenticationAcrossRegisterApplyAndCheckpoint() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("order.cart.events.v1", 1, 7L, "k", "{\"eventType\":\"CartCreated\"}");
        ParsedInboundDomainEvent event = parsedEvent(
                "evt-ctx",
                "CartCreated",
                "Cart",
                "cart-ctx",
                "organization-ctx",
                null);

        when(parser.parse(record.value())).thenReturn(event);
        when(registerUseCase.handle(any()))
                .thenAnswer(invocation -> ReactiveSecurityContextHolder.getContext()
                        .map(context -> {
                            IamSecurityPrincipal principal =
                                    IamSecurityPrincipal.fromAuthentication(context.getAuthentication());
                            assertEquals("reporting-kafka-consumer", principal.actorId());
                            assertEquals("organization-ctx", principal.organizationId());
                            return fact("fact-ctx", "evt-ctx", "CartCreated");
                        }));
        when(applyUseCase.handle(any()))
                .thenAnswer(invocation -> ReactiveSecurityContextHolder.getContext()
                        .map(context -> {
                            IamSecurityPrincipal principal =
                                    IamSecurityPrincipal.fromAuthentication(context.getAuthentication());
                            assertEquals("reporting-kafka-consumer", principal.actorId());
                            assertEquals("organization-ctx", principal.organizationId());
                            return fact("fact-ctx", "evt-ctx", "CartCreated");
                        }));
        when(checkpointUseCase.handle(any()))
                .thenAnswer(invocation -> ReactiveSecurityContextHolder.getContext()
                        .doOnNext(context -> {
                            IamSecurityPrincipal principal =
                                    IamSecurityPrincipal.fromAuthentication(context.getAuthentication());
                            assertEquals("reporting-kafka-consumer", principal.actorId());
                            assertEquals("organization-ctx", principal.organizationId());
                        })
                        .then());

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        verify(registerUseCase).handle(any());
        verify(applyUseCase).handle(any());
        verify(checkpointUseCase).handle(any());
    }

    @Test
    void shouldResolveOrganizationFromOrderLookupWhenMissingInEvent() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.events.v1", 0, 22L, "k", "{\"eventType\":\"OrderOperationalStatusUpdated\"}");
        when(parser.parse(record.value()))
                .thenReturn(parsedEvent("evt-22", "OrderOperationalStatusUpdated", "Order", "ord-22", null, null));
        when(orderOrganizationLookupHttpAdapter.resolveOrganizationByOrderId("ord-22")).thenReturn(Mono.just("organization-resolved"));
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-22", "evt-22", "OrderOperationalStatusUpdated")));
        when(applyUseCase.handle(any())).thenReturn(Mono.just(fact("fact-22", "evt-22", "OrderOperationalStatusUpdated")));
        when(checkpointUseCase.handle(any())).thenReturn(Mono.empty());

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        verify(orderOrganizationLookupHttpAdapter).resolveOrganizationByOrderId("ord-22");
    }

    @Test
    void shouldResolveOrganizationFromCartLookupForCartEvents() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("order.cart.events.v1", 0, 23L, "k", "{\"eventType\":\"CartCreated\"}");
        when(parser.parse(record.value())).thenReturn(parsedEvent("evt-23", "CartCreated", "Cart", "cart-23", null, null));
        when(orderOrganizationLookupHttpAdapter.resolveOrganizationByCartId("cart-23")).thenReturn(Mono.just("organization-cart"));
        when(registerUseCase.handle(any())).thenReturn(Mono.just(fact("fact-23", "evt-23", "CartCreated")));
        when(applyUseCase.handle(any())).thenReturn(Mono.just(fact("fact-23", "evt-23", "CartCreated")));
        when(checkpointUseCase.handle(any())).thenReturn(Mono.empty());

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        verify(orderOrganizationLookupHttpAdapter).resolveOrganizationByCartId("cart-23");
        verify(orderOrganizationLookupHttpAdapter, never()).resolveOrganizationByOrderId(any());
    }

    @Test
    void shouldSkipWhenOrganizationCannotBeResolved() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("catalog.offer-published.v1", 0, 99L, "k", "{\"eventType\":\"CatalogOfferPublished\"}");
        when(parser.parse(record.value()))
                .thenReturn(parsedEvent("evt-99", "CatalogOfferPublished", "CatalogOffer", "offer-99", null, null));

        StepVerifier.create(consumer.consume(record)).verifyComplete();

        verify(registerUseCase, never()).handle(any());
    }

    @Test
    void shouldFailWhenEventTypeMissing() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("any.topic", 0, 1L, "k", "{}");
        when(parser.parse(record.value())).thenReturn(parsedEvent("evt-x", "", "Order", "ord-x", "organization-1", null));

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
                "trace-x",
                "corr-x",
                Instant.parse("2026-04-14T10:00:00Z"),
                "{\"payload\":true}",
                "{\"payload\":true}");
    }

    private AnalyticFactResult fact(String factId, String sourceEventId, String eventType) {
        return new AnalyticFactResult(
                factId,
                "organization",
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
