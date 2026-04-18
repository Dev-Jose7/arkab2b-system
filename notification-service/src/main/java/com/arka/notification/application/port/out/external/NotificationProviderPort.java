package com.arka.notification.application.port.out.external;

import reactor.core.publisher.Mono;

public interface NotificationProviderPort {

    Mono<ProviderSendResult> send(ProviderSendRequest request);
}
