package com.arka.notification.application.port.in;

import com.arka.notification.application.query.GetNotificationMetricsQuery;
import com.arka.notification.application.result.NotificationMetricsResult;
import reactor.core.publisher.Mono;

public interface GetNotificationMetricsQueryUseCase {

    Mono<NotificationMetricsResult> handle(GetNotificationMetricsQuery query);
}
