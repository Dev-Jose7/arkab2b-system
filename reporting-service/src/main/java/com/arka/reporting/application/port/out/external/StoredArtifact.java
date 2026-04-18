package com.arka.reporting.application.port.out.external;

public record StoredArtifact(
        String locationRef,
        String contentHash,
        long sizeBytes) {
}
