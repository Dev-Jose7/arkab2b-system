package com.arka.catalog.application.query;

import java.time.Instant;

public record GetProductDetailQuery(String tenantId, String productId, Instant at) {
}
