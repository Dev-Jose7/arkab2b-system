package com.arka.order.infrastructure.adapter.out.persistence;

import com.arka.order.application.port.out.persistence.PurchaseOrderPersistencePort;
import com.arka.order.domain.order.aggregate.Order;
import com.arka.order.infrastructure.adapter.out.persistence.entity.OrderLineEntity;
import com.arka.order.infrastructure.adapter.out.persistence.entity.PaymentRecordEntity;
import com.arka.order.infrastructure.adapter.out.persistence.entity.PurchaseOrderEntity;
import com.arka.order.infrastructure.adapter.out.persistence.mapper.PurchaseOrderPersistenceMapper;
import com.arka.order.infrastructure.adapter.out.persistence.repository.OrderLineR2dbcRepository;
import com.arka.order.infrastructure.adapter.out.persistence.repository.PaymentRecordR2dbcRepository;
import com.arka.order.infrastructure.adapter.out.persistence.repository.PurchaseOrderR2dbcRepository;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PurchaseOrderR2dbcAdapter implements PurchaseOrderPersistencePort {

    private final PurchaseOrderR2dbcRepository orderRepository;
    private final OrderLineR2dbcRepository orderLineRepository;
    private final PaymentRecordR2dbcRepository paymentRecordRepository;
    private final PurchaseOrderPersistenceMapper mapper;
    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate entityTemplate;

    public PurchaseOrderR2dbcAdapter(
            PurchaseOrderR2dbcRepository orderRepository,
            OrderLineR2dbcRepository orderLineRepository,
            PaymentRecordR2dbcRepository paymentRecordRepository,
            PurchaseOrderPersistenceMapper mapper,
            DatabaseClient databaseClient,
            R2dbcEntityTemplate entityTemplate) {
        this.orderRepository = orderRepository;
        this.orderLineRepository = orderLineRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.mapper = mapper;
        this.databaseClient = databaseClient;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Boolean> existsByCheckoutCorrelation(String tenantId, String checkoutCorrelationId) {
        return orderRepository.existsByCheckoutCorrelation(tenantId, checkoutCorrelationId);
    }

    @Override
    public Mono<Order> findByCheckoutCorrelation(String tenantId, String checkoutCorrelationId) {
        return orderRepository.findByCheckoutCorrelation(tenantId, checkoutCorrelationId)
                .flatMap(this::toDomain);
    }

    @Override
    public Mono<Order> findById(String tenantId, String orderId) {
        return orderRepository.findByTenantAndOrderId(tenantId, orderId)
                .flatMap(this::toDomain);
    }

    @Override
    public Mono<Order> save(Order order) {
        PurchaseOrderEntity entity = mapper.toEntity(order);
        return orderRepository.existsById(entity.orderId())
                .flatMap(exists -> exists ? orderRepository.save(entity) : entityTemplate.insert(entity))
                .then(replaceChildren(order.orderId(), mapper.toOrderLineEntities(order), mapper.toPaymentRecordEntities(order)))
                .thenReturn(order);
    }

    @Override
    public Mono<Boolean> updateWithExpectedVersion(Order order, long expectedVersion) {
        return orderRepository.updateWithExpectedVersion(
                        order.tenantId(),
                        order.orderId(),
                        order.status().name(),
                        order.financialStatus().name(),
                        order.subtotal(),
                        order.totalAmount(),
                        order.version(),
                        expectedVersion,
                        order.updatedAt())
                .flatMap(updatedRows -> {
                    if (updatedRows == null || updatedRows <= 0) {
                        return Mono.just(Boolean.FALSE);
                    }
                    return replaceChildren(order.orderId(), mapper.toOrderLineEntities(order), mapper.toPaymentRecordEntities(order))
                            .thenReturn(Boolean.TRUE);
                });
    }

    @Override
    public Flux<Order> listByTenantOrganizationStatus(
            String tenantId,
            String organizationId,
            String status,
            Instant createdFrom,
            Instant createdTo,
            int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM purchase_orders
                WHERE tenant_id = :tenantId
                  AND organization_id = :organizationId
                """);
        if (status != null) {
            sql.append(" AND status = :status");
        }
        if (createdFrom != null) {
            sql.append(" AND created_at >= :createdFrom");
        }
        if (createdTo != null) {
            sql.append(" AND created_at <= :createdTo");
        }
        sql.append(" ORDER BY created_at DESC LIMIT :limit");

        DatabaseClient.GenericExecuteSpec query = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId)
                .bind("organizationId", organizationId)
                .bind("limit", limit);

        if (status != null) {
            query = query.bind("status", status);
        }
        if (createdFrom != null) {
            query = query.bind("createdFrom", createdFrom);
        }
        if (createdTo != null) {
            query = query.bind("createdTo", createdTo);
        }

        return query.map((row, metadata) -> mapPurchaseOrderRow(row))
                .all()
                .flatMap(this::toDomain);
    }

    private Mono<Order> toDomain(PurchaseOrderEntity entity) {
        return Mono.zip(
                        orderLineRepository.findByOrderId(entity.orderId()).collectList(),
                        paymentRecordRepository.findByOrderId(entity.orderId()).collectList())
                .map(tuple -> mapper.toDomain(entity, tuple.getT1(), tuple.getT2()));
    }

    private Mono<Void> replaceChildren(String orderId, java.util.List<OrderLineEntity> lines, java.util.List<PaymentRecordEntity> payments) {
        return orderLineRepository.deleteByOrderId(orderId)
                .then(paymentRecordRepository.deleteByOrderId(orderId))
                .thenMany(lines == null || lines.isEmpty()
                        ? Flux.empty()
                        : Flux.fromIterable(lines).concatMap(entityTemplate::insert))
                .thenMany(payments == null || payments.isEmpty()
                        ? Flux.empty()
                        : Flux.fromIterable(payments).concatMap(entityTemplate::insert))
                .then();
    }

    private PurchaseOrderEntity mapPurchaseOrderRow(Row row) {
        return new PurchaseOrderEntity(
                row.get("order_id", String.class),
                row.get("order_number", String.class),
                row.get("tenant_id", String.class),
                row.get("organization_id", String.class),
                row.get("user_id", String.class),
                row.get("cart_id", String.class),
                row.get("checkout_correlation_id", String.class),
                row.get("address_id", String.class),
                row.get("country_code", String.class),
                row.get("regional_policy_version", Long.class) == null ? 0L : row.get("regional_policy_version", Long.class),
                row.get("policy_currency", String.class),
                row.get("status", String.class),
                row.get("payment_status", String.class),
                row.get("subtotal", BigDecimal.class),
                row.get("total_amount", BigDecimal.class),
                row.get("version", Long.class) == null ? 0L : row.get("version", Long.class),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class));
    }
}
