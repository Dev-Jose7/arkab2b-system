package com.arka.notification.infrastructure.adapter.out.persistence;

import com.arka.notification.application.port.out.persistence.NotificationRequestPersistencePort;
import com.arka.notification.domain.notificationdispatch.aggregate.NotificationDispatch;
import com.arka.notification.domain.notificationdispatch.repository.NotificationDispatchRepository;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.TenantId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DomainNotificationDispatchRepositoryAdapter implements NotificationDispatchRepository {

    private final NotificationRequestPersistencePort requestPersistencePort;

    public DomainNotificationDispatchRepositoryAdapter(NotificationRequestPersistencePort requestPersistencePort) {
        this.requestPersistencePort = requestPersistencePort;
    }

    @Override
    public Mono<NotificationDispatch> save(NotificationDispatch dispatch) {
        return requestPersistencePort
                .findById(dispatch.request().tenantId(), dispatch.request().notificationId())
                .flatMap(existing -> requestPersistencePort.update(dispatch.request()))
                .switchIfEmpty(requestPersistencePort.create(dispatch.request()))
                .map(NotificationDispatch::rehydrate);
    }

    @Override
    public Mono<NotificationDispatch> findById(TenantId tenantId, NotificationId notificationId) {
        return requestPersistencePort.findById(tenantId, notificationId).map(NotificationDispatch::rehydrate);
    }

    @Override
    public Mono<NotificationDispatch> findByKey(TenantId tenantId, NotificationKey notificationKey) {
        return requestPersistencePort.findByKey(tenantId, notificationKey).map(NotificationDispatch::rehydrate);
    }
}
