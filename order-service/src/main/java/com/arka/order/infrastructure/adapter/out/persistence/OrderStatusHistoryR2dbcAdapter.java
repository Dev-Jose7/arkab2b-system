package com.arka.order.infrastructure.adapter.out.persistence;

import com.arka.order.application.port.out.persistence.OrderStatusHistoryPersistencePort;
import com.arka.order.domain.order.entity.OrderStatusHistory;
import com.arka.order.infrastructure.adapter.out.persistence.entity.OrderStatusHistoryEntity;
import com.arka.order.infrastructure.adapter.out.persistence.mapper.PurchaseOrderPersistenceMapper;
import com.arka.order.infrastructure.adapter.out.persistence.repository.OrderStatusHistoryR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrderStatusHistoryR2dbcAdapter implements OrderStatusHistoryPersistencePort {

    private final OrderStatusHistoryR2dbcRepository repository;
    private final PurchaseOrderPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    public OrderStatusHistoryR2dbcAdapter(
            OrderStatusHistoryR2dbcRepository repository,
            PurchaseOrderPersistenceMapper mapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<OrderStatusHistory> save(OrderStatusHistory statusHistory) {
        OrderStatusHistoryEntity entity = mapper.toEntity(statusHistory);
        return repository.existsById(entity.statusHistoryId())
                .flatMap(exists -> exists ? repository.save(entity) : entityTemplate.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<OrderStatusHistory> findByOrder(String organizationId, String orderId) {
        return repository.findByOrganizationAndOrderId(organizationId, orderId).map(mapper::toDomain);
    }
}
