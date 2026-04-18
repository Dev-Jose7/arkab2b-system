package com.arka.order.application.result;

import java.time.Instant;

public record OrderStatusHistoryResult(
        String statusHistoryId,
        String actorUserId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant occurredAt) {}
