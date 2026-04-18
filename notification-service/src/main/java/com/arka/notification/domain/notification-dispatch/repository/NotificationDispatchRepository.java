package com.arka.notification.domain.notificationdispatch.repository;

import com.arka.notification.domain.notificationdispatch.aggregate.NotificationDispatch;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.TenantId;
import reactor.core.publisher.Mono;

public interface NotificationDispatchRepository {

    Mono<NotificationDispatch> save(NotificationDispatch dispatch);

    Mono<NotificationDispatch> findById(TenantId tenantId, NotificationId notificationId);

    Mono<NotificationDispatch> findByKey(TenantId tenantId, NotificationKey notificationKey);
}
