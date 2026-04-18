package com.arka.catalog.infrastructure.adapter.in.web.response;

import java.time.Instant;
import java.util.List;

public record ProductResponse(
        String productId,
        String organizationId,
        String productCode,
        String name,
        String description,
        String brandId,
        String categoryId,
        String status,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt) {}
