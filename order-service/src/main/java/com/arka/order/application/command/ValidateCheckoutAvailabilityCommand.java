package com.arka.order.application.command;

public record ValidateCheckoutAvailabilityCommand(
        String organizationId,

        String userId,
        String cartId,
        String checkoutCorrelationId,
        String addressId,
        String countryCode,
        String actorUserId,
        String idempotencyKey) {}
