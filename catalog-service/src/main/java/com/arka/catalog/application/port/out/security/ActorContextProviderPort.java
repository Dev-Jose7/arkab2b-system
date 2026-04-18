package com.arka.catalog.application.port.out.security;

import reactor.core.publisher.Mono;

public interface ActorContextProviderPort {

    Mono<ActorContext> currentActor();
}
