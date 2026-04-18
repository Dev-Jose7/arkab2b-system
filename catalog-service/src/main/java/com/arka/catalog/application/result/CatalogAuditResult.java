package com.arka.catalog.application.result;

import java.util.List;

public record CatalogAuditResult(
        List<CatalogAuditEntryResult> entries,
        int page,
        int size,
        long totalElements) {
}
