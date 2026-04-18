package com.arka.notification.infrastructure.adapter.in.event;

import com.arka.notification.application.command.EmitRelevantChangeNotificationCommand;
import com.arka.notification.application.port.in.EmitRelevantChangeNotificationCommandUseCase;
import com.arka.notification.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.notification.infrastructure.adapter.in.event.InboundDomainEventParser.ParsedInboundDomainEvent;
import com.arka.notification.infrastructure.adapter.out.external.OrderContextLookupHttpAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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
    private final MeterRegistry meterRegistry;

    public OrderDomainEventKafkaConsumer(
            EmitRelevantChangeNotificationCommandUseCase emitRelevantChangeNotificationCommandUseCase,
            InboundDomainEventParser inboundDomainEventParser,
            OrderContextLookupHttpAdapter orderContextLookupHttpAdapter,
            ObjectProvider<MeterRegistry> meterRegistryProvider,
            @Value("${app.kafka.consumers.order-events.actor-id:notification-kafka-consumer}") String actorId,
            @Value("${app.kafka.consumers.order-events.channel:EMAIL}") String channel,
            @Value("${app.kafka.consumers.order-events.processing-timeout-ms:8000}") long processingTimeoutMs,
            @Value("${app.kafka.consumers.order-events.supported-event-types:OrderCreatedFromValidatedCart,OrderOperationalStatusUpdated,OrderFinancialStatusUpdated,ManualPaymentRegistered,OrderAdjustedBeforeClose,OrderConsistencyRevalidated,CartCreated,CartItemsAdjusted,CheckoutAvailabilityValidated}") String supportedEventTypes) {
        this.emitRelevantChangeNotificationCommandUseCase = emitRelevantChangeNotificationCommandUseCase;
        this.inboundDomainEventParser = inboundDomainEventParser;
        this.orderContextLookupHttpAdapter = orderContextLookupHttpAdapter;
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
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
            incrementKafkaMetric("failed", "missing-event-type");
            return Mono.error(new IllegalArgumentException("Inbound event missing eventType"));
        }
        if (!supportedOrderEventTypes.contains(event.eventType())) {
            incrementKafkaMetric("skipped", event.eventType());
            log.debug("Skipping unsupported order event. topic={} partition={} offset={} eventType={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    event.eventType());
            return Mono.empty();
        }

        return resolveContext(event)
                .flatMap(context -> {
                    String organizationId = context.organizationId();
                    String recipientRef = context.recipientRef();
                    String effectiveActorId = context.actorId();
                    Authentication authentication = technicalAuthentication(context);

                    if (organizationId == null || organizationId.isBlank()) {
                        incrementKafkaMetric("skipped", event.eventType());
                        log.warn(
                                "Skipping inbound event without organization context. topic={} partition={} offset={} eventType={} aggregateId={}",
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                event.eventType(),
                                event.aggregateId());
                        return Mono.just(Boolean.FALSE);
                    }
                    if (recipientRef == null || recipientRef.isBlank()) {
                        incrementKafkaMetric("skipped", event.eventType());
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
                            organizationId,
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
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                            .doOnSuccess(result -> log.info(
                                    "Order event consumed and notification emitted. topic={} partition={} offset={} eventType={} notificationId={}",
                                    record.topic(),
                                    record.partition(),
                                    record.offset(),
                                    event.eventType(),
                                    result.notificationId()))
                            .doOnSuccess(result -> incrementKafkaMetric("processed", event.eventType()))
                            .thenReturn(Boolean.TRUE);
                })
                .defaultIfEmpty(Boolean.FALSE)
                .flatMap(processed -> {
                    if (Boolean.TRUE.equals(processed)) {
                        return Mono.empty();
                    }
                    incrementKafkaMetric("skipped", event.eventType());
                    log.warn(
                            "Skipping inbound event without resolved context. topic={} partition={} offset={} eventType={} aggregateId={}",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            event.eventType(),
                            event.aggregateId());
                    return Mono.empty();
                })
                .doOnError(error -> incrementKafkaMetric("failed", event.eventType()))
                .then();
    }

    private Mono<ResolvedContext> resolveContext(ParsedInboundDomainEvent event) {
        String organizationId = firstNonBlank(event.organizationId(), event.organizationId());
        String recipientRef = event.organizationId();
        String parsedActorId = event.actorId();

        if ((organizationId != null && !organizationId.isBlank()) && (recipientRef != null && !recipientRef.isBlank())) {
            String effectiveActorId = parsedActorId == null || parsedActorId.isBlank() ? actorId : parsedActorId;
            return Mono.just(new ResolvedContext(organizationId, recipientRef, effectiveActorId));
        }

        if (isInventoryEvent(event)) {
            String resolvedOrganizationId = firstNonBlank(event.organizationId(), organizationId);
            String resolvedRecipientRef = firstNonBlank(recipientRef, event.organizationId(), resolvedOrganizationId);
            String resolvedActorId = firstNonBlank(parsedActorId, actorId);
            if (resolvedOrganizationId != null && !resolvedOrganizationId.isBlank()
                    && resolvedRecipientRef != null && !resolvedRecipientRef.isBlank()) {
                return Mono.just(new ResolvedContext(
                        resolvedOrganizationId,
                        resolvedRecipientRef,
                        resolvedActorId));
            }
        }

        Mono<OrderContextLookupHttpAdapter.OrderContext> lookup;
        if ("Cart".equalsIgnoreCase(event.aggregateType()) || event.eventType().startsWith("Cart")) {
            lookup = orderContextLookupHttpAdapter.resolveByCartId(event.aggregateId());
        } else {
            lookup = orderContextLookupHttpAdapter.resolveByOrderId(event.aggregateId());
        }

        return lookup
                .defaultIfEmpty(new OrderContextLookupHttpAdapter.OrderContext(null, null))
                .map(context -> {
                    String resolvedOrganizationId =
                            firstNonBlank(organizationId, context.organizationId());
                    String resolvedRecipientRef =
                            firstNonBlank(recipientRef, context.organizationId(), resolvedOrganizationId);
                    String resolvedActorId = firstNonBlank(parsedActorId, context.actorId(), actorId);
                    return new ResolvedContext(resolvedOrganizationId, resolvedRecipientRef, resolvedActorId);
                });
    }

    private boolean isInventoryEvent(ParsedInboundDomainEvent event) {
        String eventType = event.eventType() == null ? "" : event.eventType();
        String aggregateType = event.aggregateType() == null ? "" : event.aggregateType();
        return eventType.startsWith("Stock")
                || eventType.contains("Availability")
                || eventType.startsWith("Inventory")
                || "InventoryBalance".equalsIgnoreCase(aggregateType);
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

    private record ResolvedContext(
            String organizationId,
            String recipientRef,
            String actorId) {
    }

    private Authentication technicalAuthentication(ResolvedContext context) {
        IamSecurityPrincipal principal = new IamSecurityPrincipal(
                context.actorId(),
                context.organizationId(),
                "",
                Set.of("ROLE_TRUSTED_SERVICE"));
        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                Set.of(new SimpleGrantedAuthority("ROLE_TRUSTED_SERVICE")));
    }

    private void incrementKafkaMetric(String outcome, String eventType) {
        if (meterRegistry == null) {
            return;
        }
        meterRegistry
                .counter(
                        "arka.notification.kafka.order_events",
                        "outcome",
                        normalizeTag(outcome),
                        "eventType",
                        normalizeTag(eventType))
                .increment();
    }

    private String normalizeTag(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim().replace(' ', '_');
    }
}
