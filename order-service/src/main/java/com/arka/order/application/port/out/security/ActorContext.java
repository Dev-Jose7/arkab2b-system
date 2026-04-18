package com.arka.order.application.port.out.security;

public record ActorContext(
        String userId,
        String tenantId,
        String organizationId,
        boolean orderAdmin) {}
