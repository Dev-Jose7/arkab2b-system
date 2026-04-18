package com.arka.notification.application.port.in;

import com.arka.notification.application.command.RecordNotificationDeliveryCommand;
import com.arka.notification.application.result.NotificationResult;
import reactor.core.publisher.Mono;

public interface RecordNotificationDeliveryCommandUseCase {

    Mono<NotificationResult> handle(RecordNotificationDeliveryCommand command);
}
