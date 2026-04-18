package com.arka.order.infrastructure.adapter.out.persistence;

import com.arka.order.application.port.out.persistence.CheckoutAttemptPersistencePort;
import com.arka.order.domain.cart.entity.CheckoutAttempt;
import com.arka.order.infrastructure.adapter.out.persistence.entity.CheckoutAttemptEntity;
import com.arka.order.infrastructure.adapter.out.persistence.mapper.CheckoutAttemptPersistenceMapper;
import com.arka.order.infrastructure.adapter.out.persistence.repository.CheckoutAttemptR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CheckoutAttemptR2dbcAdapter implements CheckoutAttemptPersistencePort {

    private final CheckoutAttemptR2dbcRepository repository;
    private final CheckoutAttemptPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    public CheckoutAttemptR2dbcAdapter(
            CheckoutAttemptR2dbcRepository repository,
            CheckoutAttemptPersistenceMapper mapper,
            R2dbcEntityTemplate entityTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<CheckoutAttempt> save(CheckoutAttempt checkoutAttempt) {
        CheckoutAttemptEntity entity = mapper.toEntity(checkoutAttempt);
        return repository.existsById(entity.checkoutAttemptId())
                .flatMap(exists -> exists ? repository.save(entity) : entityTemplate.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<CheckoutAttempt> findByCorrelation(String tenantId, String checkoutCorrelationId) {
        return repository.findByCorrelation(tenantId, checkoutCorrelationId).map(mapper::toDomain);
    }
}
