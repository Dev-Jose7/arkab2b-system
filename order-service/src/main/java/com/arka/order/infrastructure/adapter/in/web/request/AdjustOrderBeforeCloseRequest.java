package com.arka.order.infrastructure.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AdjustOrderBeforeCloseRequest(
        @NotEmpty List<@Valid AdjustOrderLineRequest> lines,
        String reason) {}
