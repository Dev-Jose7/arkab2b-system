package com.arka.order.domain.order.entity;

import com.arka.order.domain.order.enumtype.ManualPaymentStatus;
import com.arka.order.domain.order.exception.ManualPaymentException;
import java.math.BigDecimal;
import java.time.Instant;

public record ManualPayment(
        String paymentRecordId,
        String orderId,
        String organizationId,

        String paymentReference,
        BigDecimal amount,
        String method,
        String supportReference,
        ManualPaymentStatus status,
        Instant receivedAt,
        Instant createdAt,
        Instant updatedAt) {

    public ManualPayment {
        requireNotBlank(paymentRecordId, "paymentRecordId");
        requireNotBlank(orderId, "orderId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(paymentReference, "paymentReference");
        requireNotBlank(method, "method");
        requireNotBlank(supportReference, "supportReference");
        if (amount == null || amount.signum() <= 0) {
            throw new ManualPaymentException("payment amount must be greater than zero");
        }
        status = status == null ? ManualPaymentStatus.REGISTERED : status;
        receivedAt = receivedAt == null ? Instant.now() : receivedAt;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    public ManualPayment validate(Instant now) {
        return new ManualPayment(
                paymentRecordId,
                orderId,
                organizationId,
                paymentReference,
                amount,
                method,
                supportReference,
                ManualPaymentStatus.VALIDATED,
                receivedAt,
                createdAt,
                now == null ? Instant.now() : now);
    }

    public ManualPayment reject(Instant now) {
        return new ManualPayment(
                paymentRecordId,
                orderId,
                organizationId,
                paymentReference,
                amount,
                method,
                supportReference,
                ManualPaymentStatus.REJECTED,
                receivedAt,
                createdAt,
                now == null ? Instant.now() : now);
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ManualPaymentException(fieldName + " is required");
        }
    }
}
