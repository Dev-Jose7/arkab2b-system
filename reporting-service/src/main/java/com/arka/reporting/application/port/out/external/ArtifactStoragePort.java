package com.arka.reporting.application.port.out.external;

import reactor.core.publisher.Mono;

public interface ArtifactStoragePort {

    Mono<StoredArtifact> store(
            String organizationId,
            String weekId,
            String reportType,
            String format,
            String payload);
}
