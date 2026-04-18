package com.arka.notification.application.port.in;

import com.arka.notification.application.command.DispatchNotificationCommand;
import com.arka.notification.application.result.NotificationDetailResult;
import reactor.core.publisher.Mono;

public interface DispatchNotificationCommandUseCase {

    Mono<NotificationDetailResult> handle(DispatchNotificationCommand command);
}
