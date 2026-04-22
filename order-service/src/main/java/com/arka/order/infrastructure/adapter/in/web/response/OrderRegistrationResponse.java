package com.arka.order.infrastructure.adapter.in.web.response;

public record OrderRegistrationResponse(
        String message,
        CartResponse cart,
        CheckoutAttemptResponse checkout,
        OrderResponse order) {
}
