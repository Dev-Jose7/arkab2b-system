package com.arka.catalog.application.query;

import java.time.Instant;

public record GetProductDetailQuery(String organizationId, String productId, Instant at) {
}
