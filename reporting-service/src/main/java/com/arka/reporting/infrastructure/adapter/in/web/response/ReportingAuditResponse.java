package com.arka.reporting.infrastructure.adapter.in.web.response;

import java.util.List;

public record ReportingAuditResponse(
        List<ReportingAuditEntryResponse> entries,
        int page,
        int size,
        long totalElements) {
}
