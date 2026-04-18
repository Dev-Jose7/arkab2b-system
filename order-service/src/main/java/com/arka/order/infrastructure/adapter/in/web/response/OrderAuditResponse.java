package com.arka.order.infrastructure.adapter.in.web.response;

import java.util.List;

public record OrderAuditResponse(
        String organizationId,
        String orderId,
        List<OrderAuditEntryResponse> entries) {}
