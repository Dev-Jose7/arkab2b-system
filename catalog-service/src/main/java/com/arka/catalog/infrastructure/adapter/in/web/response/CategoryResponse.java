package com.arka.catalog.infrastructure.adapter.in.web.response;

public record CategoryResponse(
        String categoryId,
        String categoryCode,
        String categoryName,
        String status) {
}
