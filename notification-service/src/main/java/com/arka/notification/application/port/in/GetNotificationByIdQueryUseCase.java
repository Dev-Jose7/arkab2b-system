package com.arka.notification.application.port.in;

import com.arka.notification.application.query.GetNotificationByIdQuery;
import com.arka.notification.application.result.NotificationResult;
import reactor.core.publisher.Mono;

public interface GetNotificationByIdQueryUseCase {

    Mono<NotificationResult> handle(GetNotificationByIdQuery query);
}
