package com.arka.notification.application.port.in;

import com.arka.notification.application.query.GetNotificationTimelineQuery;
import com.arka.notification.application.result.NotificationTimelineResult;
import reactor.core.publisher.Mono;

public interface GetNotificationTimelineQueryUseCase {

    Mono<NotificationTimelineResult> handle(GetNotificationTimelineQuery query);
}
