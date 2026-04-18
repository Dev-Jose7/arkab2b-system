package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.util.List;

public record CatalogSearchResponse(
        List<CatalogSearchItemResponse> items,
        int page,
        int size,
        long totalElements) {
}
