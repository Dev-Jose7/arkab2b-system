package com.arka.order.application.command;

import java.util.List;

public record AdjustCartItemsCommand(
        String organizationId,

        String userId,
        String cartId,
        List<AdjustCartItemInput> items,
        String actorUserId,
        String idempotencyKey) {}
