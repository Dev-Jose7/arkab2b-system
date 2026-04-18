package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.util.List;

public record FactSearchResponse(
        List<FactSearchItemResponse> items,
        int page,
        int size,
        long totalElements) {
}
