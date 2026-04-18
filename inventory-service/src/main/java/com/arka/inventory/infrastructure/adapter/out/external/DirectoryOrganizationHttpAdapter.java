package com.arka.inventory.infrastructure.adapter.out.external;

import com.arka.inventory.application.port.out.directory.OrganizationDirectoryPort;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class DirectoryOrganizationHttpAdapter implements OrganizationDirectoryPort {

    private static final String DEFAULT_ORGANIZATION_PATH = "/api/v1/internal/organizations/{organizationId}";

    private final WebClient webClient;
    private final String organizationPath;
    private final String serviceToken;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public DirectoryOrganizationHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.directory.base-url:http://directory-service}") String baseUrl,
            @Value("${app.external.directory.organization-path:}") String organizationPath,
            @Value("${app.external.directory.service-token:}") String serviceToken,
            @Value("${app.external.directory.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.directory.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.directory.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.organizationPath = organizationPath == null || organizationPath.isBlank()
                ? DEFAULT_ORGANIZATION_PATH
                : organizationPath;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public DirectoryOrganizationHttpAdapter(
            WebClient.Builder webClientBuilder,
            String baseUrl,
            String organizationPath,
            String serviceToken,
            long timeoutMs) {
        this(webClientBuilder, baseUrl, organizationPath, serviceToken, timeoutMs, 2, 200L);
    }

    @Override
    public Mono<Boolean> organizationExists(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            return Mono.just(false);
        }
        return webClient
                .get()
                .uri(organizationPath, organizationId.trim())
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
                                .flatMap(body -> Mono.error(clientError(status, organizationId, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new TransientRemoteException(
                                    "Directory organization validation failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
    }

    private RuntimeException clientError(int status, String organizationId, String body) {
        String normalizedOrganizationId = organizationId == null ? "" : organizationId.trim();
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Directory organization validation request rejected (400). organizationId="
                            + normalizedOrganizationId + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Directory organization validation unauthorized/forbidden. status=" + status + " organizationId="
                            + normalizedOrganizationId + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Directory organization validation conflict (409). organizationId=" + normalizedOrganizationId
                            + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Directory organization validation semantic error (422). organizationId="
                            + normalizedOrganizationId + " body=" + body);
            default -> new IllegalStateException(
                    "Directory organization validation client error. status=" + status + " organizationId="
                            + normalizedOrganizationId + " body=" + body);
        };
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof TimeoutException
                || throwable instanceof WebClientRequestException
                || throwable instanceof TransientRemoteException;
    }

    private static final class TransientRemoteException extends RuntimeException {
        private TransientRemoteException(String message) {
            super(message);
        }
    }
}
