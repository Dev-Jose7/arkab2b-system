package com.arka.inventory.infrastructure.adapter.out.external;

import com.arka.inventory.application.port.out.directory.TenantDirectoryPort;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class DirectoryTenantHttpAdapter implements TenantDirectoryPort {

    private static final String DEFAULT_ORGANIZATION_PATH = "/api/v1/organizations/{organizationId}";

    private final WebClient webClient;
    private final String organizationPath;
    private final String serviceToken;
    private final Duration timeout;

    public DirectoryTenantHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.directory.base-url:http://directory-service:8080}") String baseUrl,
            @Value("${app.external.directory.organization-path:}") String organizationPath,
            @Value("${app.external.directory.service-token:}") String serviceToken,
            @Value("${app.external.directory.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.organizationPath = organizationPath == null || organizationPath.isBlank()
                ? DEFAULT_ORGANIZATION_PATH
                : organizationPath;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<Boolean> tenantExists(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.just(false);
        }
        return webClient
                .get()
                .uri(organizationPath, tenantId.trim())
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.just(true);
                    }
                    int status = response.statusCode().value();
                    if (status == 404) {
                        return Mono.just(false);
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return response
                                .bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(status, tenantId, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Directory tenant validation failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(int status, String tenantId, String body) {
        String normalizedTenantId = tenantId == null ? "" : tenantId.trim();
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Directory tenant validation request rejected (400). tenantId=" + normalizedTenantId + " body="
                            + body);
            case 401, 403 -> new SecurityException(
                    "Directory tenant validation unauthorized/forbidden. status=" + status + " tenantId="
                            + normalizedTenantId + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Directory tenant validation conflict (409). tenantId=" + normalizedTenantId + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Directory tenant validation semantic error (422). tenantId=" + normalizedTenantId + " body="
                            + body);
            default -> new IllegalStateException(
                    "Directory tenant validation client error. status=" + status + " tenantId=" + normalizedTenantId
                            + " body=" + body);
        };
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }
}
