package com.arka.reporting.application.result;

import java.util.List;

public record ReportingAuditResult(
        List<ReportingAuditEntryResult> entries,
        int page,
        int size,
        long totalElements) {
}
