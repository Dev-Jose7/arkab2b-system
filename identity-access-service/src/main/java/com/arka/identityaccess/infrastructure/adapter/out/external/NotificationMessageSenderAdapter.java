package com.arka.identityaccess.infrastructure.adapter.out.external;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.port.PasswordResetMessageSender;
import com.arka.identityaccess.domain.shared.port.VerificationMessageSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class NotificationMessageSenderAdapter implements PasswordResetMessageSender, VerificationMessageSender {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String emitPath;
    private final String serviceToken;
    private final Duration timeout;

    public NotificationMessageSenderAdapter(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.external.notification.base-url:http://notification-service}") String baseUrl,
            @Value("${app.external.notification.emit-path:/api/v1/notifications}") String emitPath,
            @Value("${app.external.notification.service-token:}") String serviceToken,
            @Value("${app.external.notification.timeout-ms:3000}") long timeoutMs) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.emitPath = emitPath;
        this.serviceToken = serviceToken == null ? "" : serviceToken.trim();
        this.timeout = Duration.ofMillis(Math.max(500L, timeoutMs));
    }

    @Override
    public void sendPasswordReset(AccountId accountId, EmailAddress emailAddress, String resetToken) {
        String payloadJson = toJson(payload(
                accountId.value(),
                emailAddress.value(),
                "resetToken",
                resetToken));
        sendNotification(
                "IAM_PASSWORD_RESET",
                accountId.value(),
                payloadJson,
                "pwd-reset-" + accountId.value());
    }

    @Override
    public void sendAccountVerification(AccountId accountId, EmailAddress emailAddress, String verificationToken) {
        String payloadJson = toJson(payload(
                accountId.value(),
                emailAddress.value(),
                "verificationToken",
                verificationToken));
        sendNotification(
                "IAM_ACCOUNT_VERIFICATION",
                accountId.value(),
                payloadJson,
                "verify-" + accountId.value());
    }

    private void sendNotification(
            String sourceEventType,
            String recipientRef,
            String payloadJson,
            String correlationId) {
        NotificationEmitRequest request = new NotificationEmitRequest(
                UUID.randomUUID().toString(),
                sourceEventType,
                recipientRef,
                "EMAIL",
                payloadJson,
                correlationId,
                correlationId,
                UUID.randomUUID().toString());
        webClient
                .post()
                .uri(emitPath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(this::applyAuthHeader)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(timeout)
                .block();
    }

    private String toJson(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize notification payload", exception);
        }
    }

    private Map<String, String> payload(String accountId, String email, String tokenField, String tokenValue) {
        Map<String, String> payload = new HashMap<>();
        payload.put("accountId", accountId);
        payload.put("email", email);
        payload.put(tokenField, tokenValue);
        return payload;
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (!serviceToken.isBlank()) {
            headers.setBearerAuth(serviceToken);
        }
    }

    private record NotificationEmitRequest(
            String sourceEventId,
            String sourceEventType,
            String recipientRef,
            String channel,
            String payloadJson,
            String traceId,
            String correlationId,
            String idempotencyKey) {}
}
