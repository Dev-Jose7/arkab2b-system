package com.arka.notification.application.port.in;

import com.arka.notification.application.query.ListNotificationAttemptsQuery;
import com.arka.notification.application.result.NotificationAttemptResult;
import reactor.core.publisher.Flux;

public interface ListNotificationAttemptsQueryUseCase {

    Flux<NotificationAttemptResult> handle(ListNotificationAttemptsQuery query);
}
