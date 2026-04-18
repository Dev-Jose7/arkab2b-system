package com.arka.reporting.infrastructure.adapter.in.event;

import com.arka.reporting.application.command.ApplyAnalyticFactCommand;
import com.arka.reporting.application.command.RegisterAnalyticFactCommand;
import com.arka.reporting.application.command.UpdateConsumerCheckpointCommand;
import com.arka.reporting.application.port.in.ApplyAnalyticFactCommandUseCase;
import com.arka.reporting.application.port.in.RegisterAnalyticFactCommandUseCase;
import com.arka.reporting.application.port.in.UpdateConsumerCheckpointCommandUseCase;
import com.arka.reporting.application.result.AnalyticFactResult;
import com.arka.reporting.infrastructure.adapter.in.event.InboundDomainEventParser.ParsedInboundDomainEvent;
import com.arka.reporting.infrastructure.adapter.out.external.OrderTenantLookupHttpAdapter;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UpstreamDomainEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(UpstreamDomainEventKafkaConsumer.class);

    private final RegisterAnalyticFactCommandUseCase registerAnalyticFactCommandUseCase;
    private final ApplyAnalyticFactCommandUseCase applyAnalyticFactCommandUseCase;
    private final UpdateConsumerCheckpointCommandUseCase updateConsumerCheckpointCommandUseCase;
    private final InboundDomainEventParser inboundDomainEventParser;
    private final OrderTenantLookupHttpAdapter orderTenantLookupHttpAdapter;
    private final String actorId;
    private final String consumerName;
    private final Duration processingTimeout;

    public UpstreamDomainEventKafkaConsumer(
            RegisterAnalyticFactCommandUseCase registerAnalyticFactCommandUseCase,
            ApplyAnalyticFactCommandUseCase applyAnalyticFactCommandUseCase,
            UpdateConsumerCheckpointCommandUseCase updateConsumerCheckpointCommandUseCase,
            InboundDomainEventParser inboundDomainEventParser,
            OrderTenantLookupHttpAdapter orderTenantLookupHttpAdapter,
            @Value("${app.kafka.consumers.upstream-events.actor-id:reporting-kafka-consumer}") String actorId,
            @Value("${app.kafka.consumers.upstream-events.consumer-name:reporting-service}") String consumerName,
            @Value("${app.kafka.consumers.upstream-events.processing-timeout-ms:10000}") long processingTimeoutMs) {
        this.registerAnalyticFactCommandUseCase = registerAnalyticFactCommandUseCase;
        this.applyAnalyticFactCommandUseCase = applyAnalyticFactCommandUseCase;
        this.updateConsumerCheckpointCommandUseCase = updateConsumerCheckpointCommandUseCase;
        this.inboundDomainEventParser = inboundDomainEventParser;
        this.orderTenantLookupHttpAdapter = orderTenantLookupHttpAdapter;
        this.actorId = actorId == null || actorId.isBlank() ? "reporting-kafka-consumer" : actorId.trim();
        this.consumerName = consumerName == null || consumerName.isBlank() ? "reporting-service" : consumerName.trim();
        this.processingTimeout = Duration.ofMillis(Math.max(500L, processingTimeoutMs));
    }

    @KafkaListener(
            id = "reporting-upstream-events-consumer",
            topics = "#{'${app.kafka.consumers.upstream-events.topics:directory.regional-policy-configured.v1,directory.regional-policy-applied.v1,directory.entity-mutated.v1,catalog.offer-published.v1,catalog.offer-updated.v1,catalog.mutation.v1,inventory.stock-updated.v1,inventory.commitable-availability-recalculated.v1,inventory.mutation.v1,order.events.v1,order.cart.events.v1,notification.relevant-change-notification-emitted.v1,notification.notification-delivery-recorded.v1,notification.mutation.v1}'.split(',')}",
            groupId = "${app.kafka.consumers.upstream-events.group-id:reporting-upstream-events-consumer}",
            autoStartup = "${app.kafka.consumers.upstream-events.enabled:true}",
            containerFactory = "reportingKafkaListenerContainerFactory")
    public void onUpstreamEvent(ConsumerRecord<String, String> record) {
        consume(record).block(processingTimeout);
    }

    Mono<Void> consume(ConsumerRecord<String, String> record) {
        ParsedInboundDomainEvent event = inboundDomainEventParser.parse(record.value());
        if (event.eventType() == null || event.eventType().isBlank()) {
            return Mono.error(new IllegalArgumentException("Inbound event missing eventType"));
        }

        return resolveTenant(event)
                .flatMap(tenantId -> {
                    if (tenantId == null || tenantId.isBlank()) {
                        return Mono.just(Boolean.FALSE);
                    }
                    String factType = resolveFactType(event.eventType());
                    Instant occurredAt = event.occurredAt();

                    RegisterAnalyticFactCommand register = new RegisterAnalyticFactCommand(
                            tenantId,
                            actorId,
                            event.eventId(),
                            event.eventType(),
                            factType,
                            event.payloadJson(),
                            occurredAt,
                            consumerName,
                            "kafka-register-" + event.eventId());

                    return registerAnalyticFactCommandUseCase
                            .handle(register)
                            .flatMap(fact -> applyAndCheckpoint(record, tenantId, event.eventId(), fact))
                            .thenReturn(Boolean.TRUE);
                })
                .defaultIfEmpty(Boolean.FALSE)
                .flatMap(processed -> {
                    if (Boolean.TRUE.equals(processed)) {
                        return Mono.empty();
                    }
                    log.warn(
                            "Skipping upstream event without tenant resolution. topic={} partition={} offset={} eventType={} aggregateId={}",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            event.eventType(),
                            event.aggregateId());
                    return Mono.empty();
                });
    }

    private Mono<Void> applyAndCheckpoint(
            ConsumerRecord<String, String> record,
            String tenantId,
            String eventId,
            AnalyticFactResult fact) {
        ApplyAnalyticFactCommand apply = new ApplyAnalyticFactCommand(
                tenantId,
                actorId,
                fact.factId(),
                "kafka-apply-" + eventId);

        UpdateConsumerCheckpointCommand checkpoint = new UpdateConsumerCheckpointCommand(
                tenantId,
                actorId,
                consumerName,
                record.topic(),
                record.partition(),
                record.offset(),
                record.offset(),
                "kafka-checkpoint-" + record.topic() + "-" + record.partition() + "-" + record.offset());

        return applyAnalyticFactCommandUseCase
                .handle(apply)
                .then(updateConsumerCheckpointCommandUseCase.handle(checkpoint))
                .doOnSuccess(ignored -> log.info(
                        "Upstream event consumed for reporting. topic={} partition={} offset={} eventType={} factId={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        fact.eventType(),
                        fact.factId()))
                .then();
    }

    private Mono<String> resolveTenant(ParsedInboundDomainEvent event) {
        String directTenant = firstNonBlank(event.tenantId(), event.organizationId());
        if (directTenant != null && !directTenant.isBlank()) {
            return Mono.just(directTenant);
        }

        String eventType = event.eventType() == null ? "" : event.eventType();
        String aggregateType = event.aggregateType() == null ? "" : event.aggregateType();

        if (eventType.startsWith("Order") || eventType.startsWith("ManualPayment") || "Order".equalsIgnoreCase(aggregateType)) {
            return orderTenantLookupHttpAdapter.resolveTenantByOrderId(event.aggregateId());
        }
        if (eventType.startsWith("Cart") || eventType.startsWith("Checkout") || "Cart".equalsIgnoreCase(aggregateType)) {
            return orderTenantLookupHttpAdapter.resolveTenantByCartId(event.aggregateId());
        }

        if (eventType.startsWith("RegionalPolicy")
                || eventType.startsWith("Organization")
                || eventType.contains("Directory")) {
            return Mono.justOrEmpty(event.aggregateId());
        }

        return Mono.empty();
    }

    private String resolveFactType(String eventType) {
        String normalized = eventType == null ? "" : eventType.toLowerCase(Locale.ROOT);
        if (normalized.contains("order")
                || normalized.contains("payment")
                || normalized.contains("cart")
                || normalized.contains("checkout")) {
            return "SALES";
        }
        if (normalized.contains("inventory")
                || normalized.contains("stock")
                || normalized.contains("availability")
                || normalized.contains("replen")) {
            return "REPLENISHMENT";
        }
        if (normalized.contains("notification") || normalized.contains("delivery")) {
            return "NOTIFICATION";
        }
        if (normalized.contains("regional")
                || normalized.contains("policy")
                || normalized.contains("organization")
                || normalized.contains("directory")) {
            return "OPERATIONS";
        }
        return "GENERIC";
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
}
