package com.arka.notification.application.port.in;

import com.arka.notification.application.command.ProcessProviderCallbackCommand;
import com.arka.notification.application.result.NotificationDetailResult;
import reactor.core.publisher.Mono;

public interface ProcessProviderCallbackCommandUseCase {

    Mono<NotificationDetailResult> handle(ProcessProviderCallbackCommand command);
}
