package com.arka.directory.application.result;

import java.util.List;

public record DirectoryAuditResult(
        String organizationId,
        List<DirectoryAuditEntryResult> entries) {}
