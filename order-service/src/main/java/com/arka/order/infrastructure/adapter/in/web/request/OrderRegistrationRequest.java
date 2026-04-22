package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record OrderRegistrationRequest(
        String userId,
        String checkoutCorrelationId,
        @NotBlank(message = "addressId es obligatorio")
        String addressId,
        @NotBlank(message = "countryCode es obligatorio")
        String countryCode,
        @NotEmpty(message = "items es obligatorio")
        List<@Valid OrderRegistrationItemRequest> items) {

    public record OrderRegistrationItemRequest(
            @NotBlank(message = "variantId es obligatorio")
            String variantId,
            @NotBlank(message = "sku es obligatorio")
            String sku,
            @Positive(message = "qty debe ser mayor a cero")
            Integer qty,
            BigDecimal unitPrice,
            @NotBlank(message = "currency es obligatoria")
            String currency) {
    }
}
