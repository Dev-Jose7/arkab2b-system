package com.arka.order.infrastructure.adapter.out.persistence;

import com.arka.order.domain.order.aggregate.Order;
import com.arka.order.domain.order.repository.OrderRepository;
import com.arka.order.infrastructure.adapter.out.persistence.mapper.PurchaseOrderPersistenceMapper;
import com.arka.order.infrastructure.adapter.out.persistence.repository.OrderLineR2dbcRepository;
import com.arka.order.infrastructure.adapter.out.persistence.repository.PaymentRecordR2dbcRepository;
import com.arka.order.infrastructure.adapter.out.persistence.repository.PurchaseOrderR2dbcRepository;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DomainOrderRepositoryAdapter implements OrderRepository {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final PurchaseOrderR2dbcRepository orderRepository;
    private final OrderLineR2dbcRepository orderLineRepository;
    private final PaymentRecordR2dbcRepository paymentRecordRepository;
    private final PurchaseOrderPersistenceMapper mapper;

    public DomainOrderRepositoryAdapter(
            PurchaseOrderR2dbcRepository orderRepository,
            OrderLineR2dbcRepository orderLineRepository,
            PaymentRecordR2dbcRepository paymentRecordRepository,
            PurchaseOrderPersistenceMapper mapper) {
        this.orderRepository = orderRepository;
        this.orderLineRepository = orderLineRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return orderRepository
                .findById(orderId)
                .flatMap(entity -> orderLineRepository.findByOrderId(entity.orderId())
                        .collectList()
                        .zipWith(paymentRecordRepository.findByOrderId(entity.orderId()).collectList())
                        .map(tuple -> mapper.toDomain(entity, tuple.getT1(), tuple.getT2())))
                .blockOptional(BLOCK_TIMEOUT);
    }

    @Override
    public Order save(Order order) {
        Order persisted = orderRepository
                .save(mapper.toEntity(order))
                .flatMap(saved -> orderLineRepository.deleteByOrderId(saved.orderId())
                        .then(paymentRecordRepository.deleteByOrderId(saved.orderId()))
                        .thenMany(orderLineRepository.saveAll(mapper.toOrderLineEntities(order)))
                        .thenMany(paymentRecordRepository.saveAll(mapper.toPaymentRecordEntities(order)))
                        .then(orderLineRepository.findByOrderId(saved.orderId()).collectList())
                        .zipWith(paymentRecordRepository.findByOrderId(saved.orderId()).collectList())
                        .map(tuple -> mapper.toDomain(saved, tuple.getT1(), tuple.getT2())))
                .block(BLOCK_TIMEOUT);
        if (persisted == null) {
            throw new IllegalStateException("Order persistence returned empty result");
        }
        return persisted;
    }
}
