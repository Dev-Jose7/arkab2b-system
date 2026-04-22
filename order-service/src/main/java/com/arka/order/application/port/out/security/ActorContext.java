package com.arka.order.application.port.out.security;

public record ActorContext(
        String userId,
        String organizationId,

        boolean orderAdmin,
        boolean internalActor) {}
