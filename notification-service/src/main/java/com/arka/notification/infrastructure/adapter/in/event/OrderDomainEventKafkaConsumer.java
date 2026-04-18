package com.arka.notification.infrastructure.adapter.in.event;

import com.arka.notification.application.command.EmitRelevantChangeNotificationCommand;
import com.arka.notification.application.port.in.EmitRelevantChangeNotificationCommandUseCase;
import com.arka.notification.infrastructure.adapter.in.event.InboundDomainEventParser.ParsedInboundDomainEvent;
import com.arka.notification.infrastructure.adapter.out.external.OrderContextLookupHttpAdapter;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OrderDomainEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderDomainEventKafkaConsumer.class);

    private final EmitRelevantChangeNotificationCommandUseCase emitRelevantChangeNotificationCommandUseCase;
    private final InboundDomainEventParser inboundDomainEventParser;
    private final OrderContextLookupHttpAdapter orderContextLookupHttpAdapter;
    private final String actorId;
    private final String channel;
    private final Duration processingTimeout;
    private final Set<String> supportedOrderEventTypes;

    public OrderDomainEventKafkaConsumer(
            EmitRelevantChangeNotificationCommandUseCase emitRelevantChangeNotificationCommandUseCase,
            InboundDomainEventParser inboundDomainEventParser,
            OrderContextLookupHttpAdapter orderContextLookupHttpAdapter,
            @Value("${app.kafka.consumers.order-events.actor-id:notification-kafka-consumer}") String actorId,
            @Value("${app.kafka.consumers.order-events.channel:EMAIL}") String channel,
            @Value("${app.kafka.consumers.order-events.processing-timeout-ms:8000}") long processingTimeoutMs,
            @Value("${app.kafka.consumers.order-events.supported-event-types:OrderCreatedFromValidatedCart,OrderOperationalStatusUpdated,OrderFinancialStatusUpdated,ManualPaymentRegistered,OrderAdjustedBeforeClose,OrderConsistencyRevalidated,CartCreated,CartItemsAdjusted,CheckoutAvailabilityValidated}") String supportedEventTypes) {
        this.emitRelevantChangeNotificationCommandUseCase = emitRelevantChangeNotificationCommandUseCase;
        this.inboundDomainEventParser = inboundDomainEventParser;
        this.orderContextLookupHttpAdapter = orderContextLookupHttpAdapter;
        this.actorId = actorId == null || actorId.isBlank() ? "notification-kafka-consumer" : actorId.trim();
        this.channel = channel == null || channel.isBlank() ? "EMAIL" : channel.trim().toUpperCase();
        this.processingTimeout = Duration.ofMillis(Math.max(500L, processingTimeoutMs));
        this.supportedOrderEventTypes = Arrays.stream(supportedEventTypes.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @KafkaListener(
            id = "notification-order-events-consumer",
            topics = "#{'${app.kafka.consumers.order-events.topics:order.events.v1,order.cart.events.v1}'.split(',')}",
            groupId = "${app.kafka.consumers.order-events.group-id:notification-order-events-consumer}",
            autoStartup = "${app.kafka.consumers.order-events.enabled:true}",
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void onOrderEvent(ConsumerRecord<String, String> record) {
        consume(record).block(processingTimeout);
    }

    Mono<Void> consume(ConsumerRecord<String, String> record) {
        ParsedInboundDomainEvent event = inboundDomainEventParser.parse(record.value());
        if (event.eventType() == null || event.eventType().isBlank()) {
            return Mono.error(new IllegalArgumentException("Inbound event missing eventType"));
        }
        if (!supportedOrderEventTypes.contains(event.eventType())) {
            log.debug("Skipping unsupported order event. topic={} partition={} offset={} eventType={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    event.eventType());
            return Mono.empty();
        }

        return resolveContext(event)
                .flatMap(context -> {
                    String tenantId = context.tenantId();
                    String recipientRef = context.recipientRef();
                    String effectiveActorId = context.actorId();

                    if (tenantId == null || tenantId.isBlank()) {
                        log.warn(
                                "Skipping inbound event without tenant context. topic={} partition={} offset={} eventType={} aggregateId={}",
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                event.eventType(),
                                event.aggregateId());
                        return Mono.just(Boolean.FALSE);
                    }
                    if (recipientRef == null || recipientRef.isBlank()) {
                        log.warn(
                                "Skipping inbound event without recipient context. topic={} partition={} offset={} eventType={} aggregateId={}",
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                event.eventType(),
                                event.aggregateId());
                        return Mono.just(Boolean.FALSE);
                    }

                    EmitRelevantChangeNotificationCommand command = new EmitRelevantChangeNotificationCommand(
                            tenantId,
                            effectiveActorId,
                            event.eventId(),
                            event.eventType(),
                            recipientRef,
                            channel,
                            event.payloadJson(),
                            event.traceId(),
                            event.correlationId(),
                            "kafka-emit-" + event.eventId());

                    return emitRelevantChangeNotificationCommandUseCase
                            .handle(command)
                            .doOnSuccess(result -> log.info(
                                    "Order event consumed and notification emitted. topic={} partition={} offset={} eventType={} notificationId={}",
                                    record.topic(),
                                    record.partition(),
                                    record.offset(),
                                    event.eventType(),
                                    result.notificationId()))
                            .thenReturn(Boolean.TRUE);
                })
                .defaultIfEmpty(Boolean.FALSE)
                .flatMap(processed -> {
                    if (Boolean.TRUE.equals(processed)) {
                        return Mono.empty();
                    }
                    log.warn(
                            "Skipping inbound event without resolved context. topic={} partition={} offset={} eventType={} aggregateId={}",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            event.eventType(),
                            event.aggregateId());
                    return Mono.empty();
                });
    }

    private Mono<ResolvedContext> resolveContext(ParsedInboundDomainEvent event) {
        String tenantId = event.tenantId();
        String recipientRef = event.organizationId();
        String parsedActorId = event.actorId();

        if ((tenantId != null && !tenantId.isBlank()) && (recipientRef != null && !recipientRef.isBlank())) {
            String effectiveActorId = parsedActorId == null || parsedActorId.isBlank() ? actorId : parsedActorId;
            return Mono.just(new ResolvedContext(tenantId, recipientRef, effectiveActorId));
        }

        Mono<OrderContextLookupHttpAdapter.OrderContext> lookup;
        if ("Cart".equalsIgnoreCase(event.aggregateType()) || event.eventType().startsWith("Cart")) {
            lookup = orderContextLookupHttpAdapter.resolveByCartId(event.aggregateId());
        } else {
            lookup = orderContextLookupHttpAdapter.resolveByOrderId(event.aggregateId());
        }

        return lookup
                .defaultIfEmpty(new OrderContextLookupHttpAdapter.OrderContext(null, null, null))
                .map(context -> {
                    String resolvedTenantId = firstNonBlank(tenantId, context.tenantId(), context.organizationId());
                    String resolvedRecipientRef = firstNonBlank(recipientRef, context.organizationId(), resolvedTenantId);
                    String resolvedActorId = firstNonBlank(parsedActorId, context.actorId(), actorId);
                    return new ResolvedContext(resolvedTenantId, resolvedRecipientRef, resolvedActorId);
                });
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String normalized = value.trim();
            if (!normalized.isBlank()) {
                return normalized;
            }
        }
        return null;
    }

    private record ResolvedContext(String tenantId, String recipientRef, String actorId) {
    }
}
