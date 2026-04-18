package com.arka.order.application.command;

import java.util.List;

public record AdjustOrderBeforeCloseCommand(
        String organizationId,

        String orderId,
        List<AdjustOrderLineInput> lines,
        String reason,
        String actorUserId,
        String idempotencyKey) {}
