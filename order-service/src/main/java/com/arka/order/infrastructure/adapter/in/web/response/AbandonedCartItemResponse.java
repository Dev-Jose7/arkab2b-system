package com.arka.order.infrastructure.adapter.in.web.response;

import java.math.BigDecimal;

public record AbandonedCartItemResponse(
        String cartItemId,
        String variantId,
        String sku,
        int qty,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String currency) {
}
