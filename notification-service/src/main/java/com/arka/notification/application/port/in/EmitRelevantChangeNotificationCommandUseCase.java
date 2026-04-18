package com.arka.notification.application.port.in;

import com.arka.notification.application.command.EmitRelevantChangeNotificationCommand;
import com.arka.notification.application.result.NotificationResult;
import reactor.core.publisher.Mono;

public interface EmitRelevantChangeNotificationCommandUseCase {

    Mono<NotificationResult> handle(EmitRelevantChangeNotificationCommand command);
}
