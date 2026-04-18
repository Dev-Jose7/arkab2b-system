package com.arka.notification.application.port.out.persistence;

import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
import java.time.Instant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationRequestPersistencePort {

    Mono<NotificationRequest> create(NotificationRequest request);

    Mono<NotificationRequest> update(NotificationRequest request);

    Mono<NotificationRequest> findById(OrganizationId organizationId, NotificationId notificationId);

    Mono<NotificationRequest> findByKey(OrganizationId organizationId, NotificationKey notificationKey);

    Flux<NotificationRequest> findDispatchable(Instant at, int limit);
}
