package com.arka.identityaccess.application.port.out.external;

import reactor.core.publisher.Mono;

public interface ActorLegitimacyPort {

    Mono<Boolean> isLegitimate(String actorUserId);
}
