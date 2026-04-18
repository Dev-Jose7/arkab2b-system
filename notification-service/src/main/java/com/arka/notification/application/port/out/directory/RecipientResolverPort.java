package com.arka.notification.application.port.out.directory;

import reactor.core.publisher.Mono;

public interface RecipientResolverPort {

    Mono<RecipientResolution> resolve(String tenantId, String recipientRef, String channel);
}
