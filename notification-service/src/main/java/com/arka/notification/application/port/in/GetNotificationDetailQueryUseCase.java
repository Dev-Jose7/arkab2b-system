package com.arka.notification.application.port.in;

import com.arka.notification.application.query.GetNotificationDetailQuery;
import com.arka.notification.application.result.NotificationDetailResult;
import reactor.core.publisher.Mono;

public interface GetNotificationDetailQueryUseCase {

    Mono<NotificationDetailResult> handle(GetNotificationDetailQuery query);
}
