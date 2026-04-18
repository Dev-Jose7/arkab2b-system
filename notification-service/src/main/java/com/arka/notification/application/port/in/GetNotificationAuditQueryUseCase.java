package com.arka.notification.application.port.in;

import com.arka.notification.application.query.GetNotificationAuditQuery;
import com.arka.notification.application.result.NotificationAuditResult;
import reactor.core.publisher.Mono;

public interface GetNotificationAuditQueryUseCase {

    Mono<NotificationAuditResult> handle(GetNotificationAuditQuery query);
}
