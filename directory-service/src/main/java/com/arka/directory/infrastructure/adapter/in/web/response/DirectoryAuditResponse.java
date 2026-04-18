package com.arka.directory.infrastructure.adapter.in.web.response;

import java.util.List;

public record DirectoryAuditResponse(
        String organizationId,
        List<DirectoryAuditEntryResponse> entries) {}
