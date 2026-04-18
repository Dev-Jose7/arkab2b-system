package com.arka.notification.application.port.in;

import com.arka.notification.application.command.DiscardNotificationCommand;
import com.arka.notification.application.result.NotificationResult;
import reactor.core.publisher.Mono;

public interface DiscardNotificationCommandUseCase {

    Mono<NotificationResult> handle(DiscardNotificationCommand command);
}
