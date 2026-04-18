package com.arka.order.application.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResult(
        String orderId,
        String orderNumber,
        String tenantId,
        String organizationId,
        String userId,
        String cartId,
        String checkoutCorrelationId,
        String addressId,
        String countryCode,
        long regionalPolicyVersion,
        String policyCurrency,
        String status,
        String financialStatus,
        BigDecimal subtotal,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        long version,
        Instant createdAt,
        Instant updatedAt,
        List<OrderLineResult> lines,
        List<ManualPaymentResult> payments) {}
