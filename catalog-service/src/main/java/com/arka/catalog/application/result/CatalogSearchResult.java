package com.arka.catalog.application.result;

import java.util.List;

public record CatalogSearchResult(
        List<CatalogSearchItemResult> items,
        int page,
        int size,
        long totalElements) {
}
