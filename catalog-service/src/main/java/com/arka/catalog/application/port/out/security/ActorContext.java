package com.arka.catalog.application.port.out.security;

public record ActorContext(
        String actorId,
        String organizationId,
        String countryCode,
        boolean admin,
        boolean internalActor) {
}
