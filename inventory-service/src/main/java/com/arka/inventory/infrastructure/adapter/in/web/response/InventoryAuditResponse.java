package com.arka.inventory.infrastructure.adapter.in.web.response;

import java.util.List;

public record InventoryAuditResponse(
        String tenantId,
        List<InventoryAuditEntryResponse> entries) {}
