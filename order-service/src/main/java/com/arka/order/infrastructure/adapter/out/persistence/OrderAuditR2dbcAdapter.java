package com.arka.order.infrastructure.adapter.out.persistence;

import com.arka.order.application.port.out.audit.OrderAuditPort;
import com.arka.order.application.result.OrderAuditEntryResult;
import com.arka.order.infrastructure.adapter.out.persistence.entity.OrderAuditEntity;
import com.arka.order.infrastructure.adapter.out.persistence.repository.OrderAuditR2dbcRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrderAuditR2dbcAdapter implements OrderAuditPort {

    private final OrderAuditR2dbcRepository repository;
    private final R2dbcEntityTemplate entityTemplate;

    public OrderAuditR2dbcAdapter(
            OrderAuditR2dbcRepository repository,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Void> record(
            String tenantId,
            String organizationId,
            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String outcome,
            String payload) {
        OrderAuditEntity entity = new OrderAuditEntity(
                UUID.randomUUID().toString(),
                tenantId,
                organizationId,
                actorUserId,
                actionType,
                targetType,
                targetId,
                outcome,
                payload,
                Instant.now());
        return entityTemplate.insert(entity).then();
    }

    @Override
    public Flux<OrderAuditEntryResult> findByOrder(String tenantId, String organizationId, String orderId, int limit) {
        return repository.findByOrder(tenantId, organizationId, orderId, limit)
                .map(entity -> new OrderAuditEntryResult(
                        entity.auditId(),
                        entity.tenantId(),
                        entity.organizationId(),
                        entity.actorUserId(),
                        entity.actionType(),
                        entity.targetType(),
                        entity.targetId(),
                        entity.outcome(),
                        entity.payload(),
                        entity.createdAt()));
    }
}
