package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CustomerOrderUpdateRequest(
        @NotEmpty(message = "lines es obligatorio")
        List<@Valid AdjustOrderLineRequest> lines,
        String reason,
        Boolean revalidateAfterAdjustment) {
}
