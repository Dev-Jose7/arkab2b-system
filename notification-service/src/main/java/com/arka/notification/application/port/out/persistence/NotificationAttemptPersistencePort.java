package com.arka.notification.application.port.out.persistence;

import com.arka.notification.domain.notificationdispatch.entity.NotificationAttempt;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationAttemptPersistencePort {

    Mono<NotificationAttempt> create(NotificationAttempt attempt, String organizationId);

    Mono<NotificationAttempt> update(NotificationAttempt attempt, String organizationId);

    Flux<NotificationAttempt> findByNotificationId(OrganizationId organizationId, NotificationId notificationId);
}
