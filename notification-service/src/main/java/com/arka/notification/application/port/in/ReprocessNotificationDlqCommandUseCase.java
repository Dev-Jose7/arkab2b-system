package com.arka.notification.application.port.in;

import com.arka.notification.application.command.ReprocessNotificationDlqCommand;
import com.arka.notification.application.result.NotificationDetailResult;
import reactor.core.publisher.Mono;

public interface ReprocessNotificationDlqCommandUseCase {

    Mono<NotificationDetailResult> handle(ReprocessNotificationDlqCommand command);
}
