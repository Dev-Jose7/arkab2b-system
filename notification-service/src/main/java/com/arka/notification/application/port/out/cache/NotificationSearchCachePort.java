package com.arka.notification.application.port.out.cache;

import com.arka.notification.application.result.NotificationSearchResult;
import reactor.core.publisher.Mono;

public interface NotificationSearchCachePort {

    Mono<NotificationSearchResult> get(String cacheKey);

    Mono<Void> put(String cacheKey, NotificationSearchResult result);

    Mono<Void> evictOrganization(String organizationId);
}
