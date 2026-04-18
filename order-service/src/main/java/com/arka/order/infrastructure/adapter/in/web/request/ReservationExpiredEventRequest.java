package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record ReservationExpiredEventRequest(
        @NotBlank String organizationId,
        @NotBlank String reservationId,
        @NotBlank String eventId) {}
