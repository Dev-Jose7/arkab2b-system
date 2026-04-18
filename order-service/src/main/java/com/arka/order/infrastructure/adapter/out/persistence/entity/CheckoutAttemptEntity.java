package com.arka.order.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("checkout_attempts")
public record CheckoutAttemptEntity(
        @Id String checkoutAttemptId,
        String organizationId,

        String userId,
        String cartId,
        String checkoutCorrelationId,
        String validationStatus,
        String addressId,
        String countryCode,
        long regionalPolicyVersion,
        String policyCurrency,
        String rejectionReasons,
        Instant createdAt,
        Instant updatedAt) {}
