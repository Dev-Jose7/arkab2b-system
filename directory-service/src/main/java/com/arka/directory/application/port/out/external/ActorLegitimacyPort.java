package com.arka.directory.application.port.out.external;

import reactor.core.publisher.Mono;

public interface ActorLegitimacyPort {

    Mono<Boolean> isLegitimate(String actorUserId);
}
