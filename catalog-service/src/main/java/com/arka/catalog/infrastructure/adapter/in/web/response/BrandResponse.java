package com.arka.catalog.infrastructure.adapter.in.web.response;

public record BrandResponse(
        String brandId,
        String brandCode,
        String brandName,
        String status) {
}
