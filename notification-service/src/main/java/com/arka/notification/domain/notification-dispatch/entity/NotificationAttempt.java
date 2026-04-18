package com.arka.notification.domain.notificationdispatch.entity;

import com.arka.notification.domain.notificationdispatch.enumtype.NotificationAttemptResultStatus;
import com.arka.notification.domain.notificationdispatch.exception.NotificationDomainException;
import com.arka.notification.domain.notificationdispatch.valueobject.AttemptId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import java.time.Instant;

public final class NotificationAttempt {

    private final AttemptId attemptId;
    private final NotificationId notificationId;
    private final int attemptNumber;
    private final Instant createdAt;

    private NotificationAttemptResultStatus resultStatus;
    private String providerCode;
    private String providerRef;
    private String errorCode;
    private String errorMessage;
    private boolean retryable;
    private Long latencyMs;
    private String requestSnapshot;
    private String responseSnapshot;

    private NotificationAttempt(
            AttemptId attemptId,
            NotificationId notificationId,
            int attemptNumber,
            NotificationAttemptResultStatus resultStatus,
            String providerCode,
            String providerRef,
            String errorCode,
            String errorMessage,
            boolean retryable,
            Long latencyMs,
            String requestSnapshot,
            String responseSnapshot,
            Instant createdAt) {
        this.attemptId = attemptId;
        this.notificationId = notificationId;
        this.attemptNumber = attemptNumber;
        this.resultStatus = resultStatus;
        this.providerCode = normalize(providerCode);
        this.providerRef = normalize(providerRef);
        this.errorCode = normalize(errorCode);
        this.errorMessage = normalize(errorMessage);
        this.retryable = retryable;
        this.latencyMs = latencyMs;
        this.requestSnapshot = requestSnapshot == null ? "" : requestSnapshot.trim();
        this.responseSnapshot = responseSnapshot == null ? "" : responseSnapshot.trim();
        this.createdAt = createdAt;
    }

    public static NotificationAttempt created(
            AttemptId attemptId,
            NotificationId notificationId,
            int attemptNumber,
            String providerCode,
            String requestSnapshot,
            Instant now) {
        if (attemptNumber <= 0) {
            throw new NotificationDomainException("attempt_number_invalido", "attempt_number debe ser mayor que cero");
        }
        return new NotificationAttempt(
                attemptId,
                notificationId,
                attemptNumber,
                NotificationAttemptResultStatus.CREATED,
                providerCode,
                null,
                null,
                null,
                true,
                null,
                requestSnapshot,
                null,
                now);
    }

    public static NotificationAttempt rehydrate(
            AttemptId attemptId,
            NotificationId notificationId,
            int attemptNumber,
            NotificationAttemptResultStatus resultStatus,
            String providerCode,
            String providerRef,
            String errorCode,
            String errorMessage,
            boolean retryable,
            Long latencyMs,
            String requestSnapshot,
            String responseSnapshot,
            Instant createdAt) {
        return new NotificationAttempt(
                attemptId,
                notificationId,
                attemptNumber,
                resultStatus,
                providerCode,
                providerRef,
                errorCode,
                errorMessage,
                retryable,
                latencyMs,
                requestSnapshot,
                responseSnapshot,
                createdAt);
    }

    public void markSent(String providerRef, Long latencyMs, String responseSnapshot) {
        ensureMutable();
        this.resultStatus = NotificationAttemptResultStatus.SENT;
        this.providerRef = normalize(providerRef);
        this.latencyMs = latencyMs;
        this.responseSnapshot = responseSnapshot == null ? "" : responseSnapshot.trim();
        this.retryable = false;
        this.errorCode = "";
        this.errorMessage = "";
    }

    public void markFailed(
            String errorCode,
            String errorMessage,
            boolean retryable,
            Long latencyMs,
            String responseSnapshot) {
        ensureMutable();
        this.resultStatus = NotificationAttemptResultStatus.FAILED;
        this.errorCode = normalize(errorCode);
        this.errorMessage = normalize(errorMessage);
        this.retryable = retryable;
        this.latencyMs = latencyMs;
        this.responseSnapshot = responseSnapshot == null ? "" : responseSnapshot.trim();
    }

    private void ensureMutable() {
        if (resultStatus != NotificationAttemptResultStatus.CREATED) {
            throw new NotificationDomainException(
                    "attempt_no_mutable",
                    "Solo se puede actualizar un intento en estado CREATED");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public AttemptId attemptId() {
        return attemptId;
    }

    public NotificationId notificationId() {
        return notificationId;
    }

    public int attemptNumber() {
        return attemptNumber;
    }

    public NotificationAttemptResultStatus resultStatus() {
        return resultStatus;
    }

    public String providerCode() {
        return providerCode;
    }

    public String providerRef() {
        return providerRef;
    }

    public String errorCode() {
        return errorCode;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public boolean retryable() {
        return retryable;
    }

    public Long latencyMs() {
        return latencyMs;
    }

    public String requestSnapshot() {
        return requestSnapshot;
    }

    public String responseSnapshot() {
        return responseSnapshot;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
