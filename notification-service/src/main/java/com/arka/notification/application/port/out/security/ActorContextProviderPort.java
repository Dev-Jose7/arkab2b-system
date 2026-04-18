package com.arka.notification.application.port.out.security;

import reactor.core.publisher.Mono;

public interface ActorContextProviderPort {

    Mono<ActorContext> currentActor();
}
