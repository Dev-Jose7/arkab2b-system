package com.arka.order.infrastructure.adapter.out.external;

import com.arka.order.application.port.out.external.ActorLegitimacyPort;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class IdentityActorLegitimacyHttpAdapter implements ActorLegitimacyPort {

    private final WebClient webClient;
    private final String path;
    private final String serviceToken;
    private final Duration timeout;

    public IdentityActorLegitimacyHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.external.identity.base-url:http://identity-access-service:8080}") String baseUrl,
            @Value("${app.external.identity.actor-legitimacy-path:/api/v1/admin/iam/users/{actorId}/permissions}") String path,
            @Value("${app.external.identity.service-token:}") String serviceToken,
            @Value("${app.external.identity.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.path = path;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public Mono<Boolean> isLegitimate(String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            return Mono.just(false);
        }
        String normalizedActorId = actorUserId.trim();
        return webClient
                .get()
                .uri(path, normalizedActorId)
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
                                .flatMap(body -> Mono.error(clientError(status, normalizedActorId, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new IllegalStateException(
                                    "Identity legitimacy validation failed status="
                                            + status
                                            + " body="
                                            + body)));
                })
                .timeout(timeout);
    }

    private RuntimeException clientError(int status, String actorId, String body) {
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Identity legitimacy request rejected (400). actorId=" + actorId + " body=" + body);
            case 401, 403 -> new SecurityException(
                    "Identity legitimacy unauthorized/forbidden. status=" + status + " actorId=" + actorId
                            + " body=" + body);
            case 409 -> new IllegalStateException(
                    "Identity legitimacy conflict (409). actorId=" + actorId + " body=" + body);
            case 422 -> new IllegalStateException(
                    "Identity legitimacy semantic error (422). actorId=" + actorId + " body=" + body);
            default -> new IllegalStateException(
                    "Identity legitimacy client error. status=" + status + " actorId=" + actorId + " body="
                            + body);
        };
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }
}
