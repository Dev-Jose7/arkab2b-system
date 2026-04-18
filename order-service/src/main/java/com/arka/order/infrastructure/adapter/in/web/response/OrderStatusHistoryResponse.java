package com.arka.order.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record OrderStatusHistoryResponse(
        String statusHistoryId,
        String actorUserId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant occurredAt) {}
