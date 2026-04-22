package com.arka.order.infrastructure.adapter.in.web.response;

public record CustomerOrderUpdateResponse(
        String message,
        boolean revalidated,
        OrderResponse order) {
}
