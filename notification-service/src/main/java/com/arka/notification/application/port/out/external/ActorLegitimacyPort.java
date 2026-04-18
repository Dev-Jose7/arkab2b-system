package com.arka.notification.application.port.out.external;

import reactor.core.publisher.Mono;

public interface ActorLegitimacyPort {

    Mono<Boolean> isLegitimate(String actorId, String organizationId);
}
