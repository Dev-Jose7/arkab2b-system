package com.arka.inventory.application.result;

import java.util.List;

public record InventoryAuditResult(
        String organizationId,
        List<InventoryAuditEntryResult> entries) {}
