package com.arka.notification.application.port.in;

import com.arka.notification.application.query.SearchNotificationsQuery;
import com.arka.notification.application.result.NotificationSearchResult;
import reactor.core.publisher.Mono;

public interface SearchNotificationsQueryUseCase {

    Mono<NotificationSearchResult> handle(SearchNotificationsQuery query);
}
