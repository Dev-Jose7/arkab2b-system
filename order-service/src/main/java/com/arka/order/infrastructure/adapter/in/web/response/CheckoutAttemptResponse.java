package com.arka.order.infrastructure.adapter.in.web.response;

import java.time.Instant;
import java.util.List;

public record CheckoutAttemptResponse(
        String checkoutAttemptId,
        String checkoutCorrelationId,
        String tenantId,
        String organizationId,
        String userId,
        String cartId,
        String validationStatus,
        String addressId,
        String countryCode,
        long regionalPolicyVersion,
        String policyCurrency,
        List<String> rejectionReasons,
        Instant createdAt,
        Instant updatedAt) {}
