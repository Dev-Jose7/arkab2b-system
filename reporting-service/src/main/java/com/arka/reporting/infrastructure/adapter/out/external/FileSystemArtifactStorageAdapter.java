package com.arka.reporting.infrastructure.adapter.out.external;

import com.arka.reporting.application.port.out.external.ArtifactStoragePort;
import com.arka.reporting.application.port.out.external.StoredArtifact;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class FileSystemArtifactStorageAdapter implements ArtifactStoragePort {

    private final Path basePath;

    public FileSystemArtifactStorageAdapter(
            @Value("${app.external.artifact-storage.base-path:./build/artifacts/reporting}") String basePath) {
        this.basePath = Paths.get(basePath).normalize();
    }

    @Override
    public Mono<StoredArtifact> store(
            String organizationId,
            String weekId,
            String reportType,
            String format,
            String payload) {
        return Mono.fromCallable(() -> doStore(organizationId, weekId, reportType, format, payload))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private StoredArtifact doStore(String organizationId, String weekId, String reportType, String format, String payload) throws Exception {
        String safeOrganization = sanitize(organizationId, "unknown-organization");
        String safeWeek = sanitize(weekId, "unknown-week");
        String safeType = sanitize(reportType, "generic");
        String safeFormat = sanitize(format, "json").toLowerCase(Locale.ROOT);
        String safePayload = payload == null ? "" : payload;
        byte[] bytes = safePayload.getBytes(StandardCharsets.UTF_8);

        Path folder = basePath.resolve(safeOrganization).resolve(safeWeek).resolve(safeType);
        Files.createDirectories(folder);

        String fileName = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "." + safeFormat;
        Path file = folder.resolve(fileName);
        Files.write(file, bytes);

        String hash = sha256Hex(bytes);
        String locationRef = file.toAbsolutePath().toString();
        return new StoredArtifact(locationRef, hash, bytes.length);
    }

    private String sanitize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(bytes));
    }
}
