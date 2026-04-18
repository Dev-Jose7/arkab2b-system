package com.arka.notification.application.port.out.persistence;

import com.arka.notification.domain.notificationdispatch.entity.ProviderCallback;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProviderCallbackPersistencePort {

    Mono<ProviderCallback> create(ProviderCallback callback);

    Mono<ProviderCallback> findByProviderRefAndEvent(String providerCode, String providerRef, String callbackEventId);

    Flux<ProviderCallbackProjection> findByNotificationId(String tenantId, String notificationId);
}
