package com.arka.notification.infrastructure.adapter.out.persistence;

import com.arka.notification.application.port.out.persistence.NotificationRequestPersistencePort;
import com.arka.notification.domain.notificationdispatch.aggregate.NotificationDispatch;
import com.arka.notification.domain.notificationdispatch.repository.NotificationDispatchRepository;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
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
                .findById(dispatch.request().organizationId(), dispatch.request().notificationId())
                .flatMap(existing -> requestPersistencePort.update(dispatch.request()))
                .switchIfEmpty(requestPersistencePort.create(dispatch.request()))
                .map(NotificationDispatch::rehydrate);
    }

    @Override
    public Mono<NotificationDispatch> findById(OrganizationId organizationId, NotificationId notificationId) {
        return requestPersistencePort.findById(organizationId, notificationId).map(NotificationDispatch::rehydrate);
    }

    @Override
    public Mono<NotificationDispatch> findByKey(OrganizationId organizationId, NotificationKey notificationKey) {
        return requestPersistencePort.findByKey(organizationId, notificationKey).map(NotificationDispatch::rehydrate);
    }
}
