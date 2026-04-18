package com.arka.reporting.application.result;

import java.util.List;

public record FactSearchResult(
        List<FactSearchItemResult> items,
        int page,
        int size,
        long totalElements) {
}
