package com.arka.order.application.result;

import java.time.Instant;
import java.util.List;

public record CheckoutAttemptResult(
        String checkoutAttemptId,
        String checkoutCorrelationId,
        String tenantId,
        String organizationId,
        String userId,
        String cartId,
        String validationStatus,
        String addressId,
        String countryCode,
        Long regionalPolicyVersion,
        String policyCurrency,
        List<String> rejectionReasons,
        Instant createdAt,
        Instant updatedAt) {}
