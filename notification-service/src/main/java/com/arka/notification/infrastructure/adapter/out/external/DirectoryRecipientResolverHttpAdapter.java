package com.arka.notification.infrastructure.adapter.out.external;

import com.arka.notification.application.port.out.directory.RecipientResolution;
import com.arka.notification.application.port.out.directory.RecipientResolverPort;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.Iterator;
import java.util.Locale;
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
public class DirectoryRecipientResolverHttpAdapter implements RecipientResolverPort {

    private final WebClient webClient;
    private final String contactsPath;
    private final String serviceToken;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryBackoff;

    @Autowired
    public DirectoryRecipientResolverHttpAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${app.external.directory.base-url:http://directory-service}") String baseUrl,
            @Value("${app.external.directory.contacts-path:/api/v1/internal/organizations/{organizationId}/contacts}") String contactsPath,
            @Value("${app.external.directory.service-token:}") String serviceToken,
            @Value("${app.external.directory.timeout-ms:3000}") long timeoutMs,
            @Value("${app.external.directory.retry.max-attempts:2}") int maxRetryAttempts,
            @Value("${app.external.directory.retry.backoff-ms:200}") long retryBackoffMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.contactsPath = contactsPath;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
        this.maxRetryAttempts = Math.max(0, maxRetryAttempts);
        this.retryBackoff = Duration.ofMillis(Math.max(50L, retryBackoffMs));
    }

    public DirectoryRecipientResolverHttpAdapter(
            WebClient.Builder webClientBuilder,
            String baseUrl,
            String contactsPath,
            String serviceToken,
            long timeoutMs) {
        this(webClientBuilder, baseUrl, contactsPath, serviceToken, timeoutMs, 2, 200L);
    }

    @Override
    public Mono<RecipientResolution> resolve(String organizationId, String recipientRef, String channel) {
        if (organizationId == null || organizationId.isBlank()
                || recipientRef == null || recipientRef.isBlank()
                || channel == null || channel.isBlank()) {
            return Mono.empty();
        }
        String normalizedChannel = channel.trim().toUpperCase(Locale.ROOT);
        return webClient
                .get()
                .uri(contactsPath, recipientRef.trim())
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(JsonNode.class)
                                .flatMap(body -> resolveDestination(recipientRef.trim(), normalizedChannel, body));
                    }
                    int status = response.statusCode().value();
                    if (status == 404) {
                        return Mono.empty();
                    }
                    if (response.statusCode().is4xxClientError()) {
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(clientError(status, recipientRef, channel, body)));
                    }
                    return response
                            .bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new TransientRemoteException(
                                    "Recipient resolution failed status="
                                            + response.statusCode().value()
                                            + " body="
                                            + body)));
                })
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryBackoff).filter(this::isRetryable));
    }

    private Mono<RecipientResolution> resolveDestination(String recipientRef, String channel, JsonNode body) {
        if (body == null || !body.isArray()) {
            return Mono.empty();
        }
        String requiredType = channelToContactType(channel);
        Iterator<JsonNode> iterator = body.elements();
        while (iterator.hasNext()) {
            JsonNode contact = iterator.next();
            String status = text(contact, "status");
            String contactType = text(contact, "contactType");
            String value = text(contact, "value");
            if (!"ACTIVE".equalsIgnoreCase(status)) {
                continue;
            }
            if (requiredType != null && !requiredType.equalsIgnoreCase(contactType)) {
                continue;
            }
            if (value != null && !value.isBlank()) {
                return Mono.just(new RecipientResolution(recipientRef, channel, value.trim(), true));
            }
        }
        return Mono.empty();
    }

    private String channelToContactType(String channel) {
        return switch (channel) {
            case "EMAIL" -> "EMAIL";
            case "SMS", "WHATSAPP" -> "PHONE";
            default -> null;
        };
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }

    private RuntimeException clientError(int status, String recipientRef, String channel, String body) {
        String normalizedRecipient = recipientRef == null ? "" : recipientRef.trim();
        String normalizedChannel = channel == null ? "" : channel.trim().toUpperCase(Locale.ROOT);
        return switch (status) {
            case 400 -> new IllegalArgumentException(
                    "Recipient resolution request rejected (400). recipientRef="
                            + normalizedRecipient
                            + " channel="
                            + normalizedChannel
                            + " body="
                            + body);
            case 401, 403 -> new SecurityException(
                    "Recipient resolution unauthorized/forbidden. status="
                            + status
                            + " recipientRef="
                            + normalizedRecipient
                            + " channel="
                            + normalizedChannel
                            + " body="
                            + body);
            case 409 -> new IllegalStateException(
                    "Recipient resolution conflict (409). recipientRef="
                            + normalizedRecipient
                            + " channel="
                            + normalizedChannel
                            + " body="
                            + body);
            case 422 -> new IllegalStateException(
                    "Recipient resolution semantic error (422). recipientRef="
                            + normalizedRecipient
                            + " channel="
                            + normalizedChannel
                            + " body="
                            + body);
            default -> new IllegalStateException(
                    "Recipient resolution client error. status="
                            + status
                            + " recipientRef="
                            + normalizedRecipient
                            + " channel="
                            + normalizedChannel
                            + " body="
                            + body);
        };
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
