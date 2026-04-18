package com.arka.notification.application.port.in;

import com.arka.notification.application.command.RetryNotificationCommand;
import com.arka.notification.application.result.NotificationDetailResult;
import reactor.core.publisher.Mono;

public interface RetryNotificationCommandUseCase {

    Mono<NotificationDetailResult> handle(RetryNotificationCommand command);
}
