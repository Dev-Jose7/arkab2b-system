package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.util.List;

public record CatalogAuditResponse(
        List<CatalogAuditEntryResponse> entries,
        int page,
        int size,
        long totalElements) {
}
