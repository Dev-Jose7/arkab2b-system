package com.arka.order.domain.cart.entity;

import com.arka.order.domain.cart.enumtype.CheckoutValidationStatus;
import com.arka.order.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.List;

public record CheckoutAttempt(
        String checkoutAttemptId,
        String tenantId,
        String organizationId,
        String userId,
        String cartId,
        String checkoutCorrelationId,
        CheckoutValidationStatus validationStatus,
        String addressId,
        String countryCode,
        Long regionalPolicyVersion,
        String policyCurrency,
        List<String> rejectionReasons,
        Instant createdAt,
        Instant updatedAt) {

    public CheckoutAttempt {
        requireNotBlank(checkoutAttemptId, "checkoutAttemptId");
        requireNotBlank(tenantId, "tenantId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(userId, "userId");
        requireNotBlank(cartId, "cartId");
        requireNotBlank(checkoutCorrelationId, "checkoutCorrelationId");
        requireNotBlank(countryCode, "countryCode");
        if (validationStatus == null) {
            throw new DomainInvariantViolationException("validationStatus is required");
        }
        if (regionalPolicyVersion == null || regionalPolicyVersion <= 0) {
            throw new DomainInvariantViolationException("regionalPolicyVersion must be positive");
        }
        rejectionReasons = rejectionReasons == null ? List.of() : List.copyOf(rejectionReasons);
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}
