package com.arka.notification.domain.notificationdispatch.aggregate;

import com.arka.notification.domain.notificationdispatch.entity.ChannelPolicy;
import com.arka.notification.domain.notificationdispatch.entity.NotificationAttempt;
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.event.NotificationDeliveryRecorded;
import com.arka.notification.domain.notificationdispatch.event.RelevantChangeNotificationEmitted;
import com.arka.notification.domain.notificationdispatch.exception.AttemptNumberSequenceException;
import com.arka.notification.domain.notificationdispatch.exception.DiscardedNotificationCannotDispatchException;
import com.arka.notification.domain.notificationdispatch.valueobject.AttemptId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.RelevantChangeNotification;
import com.arka.notification.domain.shared.event.DomainEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class NotificationDispatch {

    private final NotificationRequest request;
    private final List<DomainEvent> domainEvents;

    private NotificationDispatch(NotificationRequest request, List<DomainEvent> domainEvents) {
        this.request = request;
        this.domainEvents = domainEvents;
    }

    public static NotificationDispatch emit(
            RelevantChangeNotification relevantChangeNotification,
            String templateId,
            String channelPolicyId,
            String renderedPayload,
            int maxAttempts,
            Instant firstRetryAt,
            Instant now) {
        NotificationId notificationId = NotificationId.newId();
        NotificationKey notificationKey = NotificationKey.fromEventRecipientAndChannel(
                relevantChangeNotification.sourceEventId(),
                relevantChangeNotification.recipientRef(),
                relevantChangeNotification.channel());

        NotificationRequest request = NotificationRequest.createPending(
                notificationId,
                relevantChangeNotification.organizationId(),
                relevantChangeNotification.sourceEventId(),
                relevantChangeNotification.sourceEventType(),
                relevantChangeNotification.recipientRef(),
                relevantChangeNotification.channel(),
                notificationKey,
                templateId,
                channelPolicyId,
                renderedPayload,
                maxAttempts,
                firstRetryAt,
                relevantChangeNotification.traceId(),
                relevantChangeNotification.correlationId(),
                now);

        List<DomainEvent> events = new ArrayList<>();
        events.add(new RelevantChangeNotificationEmitted(
                request.notificationId().value(),
                request.organizationId().value(),
                request.sourceEventId(),
                request.sourceEventType(),
                request.recipientRef(),
                request.channel().name(),
                now));

        return new NotificationDispatch(request, events);
    }

    public static NotificationDispatch rehydrate(NotificationRequest request) {
        return new NotificationDispatch(request, new ArrayList<>());
    }

    public NotificationAttempt beginDispatchAttempt(ChannelPolicy channelPolicy, Instant now) {
        if (request.status().name().equals("DISCARDED")) {
            throw new DiscardedNotificationCannotDispatchException();
        }
        if (!request.canDispatchAt(now)) {
            throw new DiscardedNotificationCannotDispatchException();
        }
        int nextAttemptNumber = request.attemptCount() + 1;
        if (nextAttemptNumber <= 0) {
            throw new AttemptNumberSequenceException(1, nextAttemptNumber);
        }
        request.registerAttempt(nextAttemptNumber, now);
        boolean retryAttempt = nextAttemptNumber > 1;
        var dispatchChannel = retryAttempt && channelPolicy.fallbackChannel() != null
                ? channelPolicy.fallbackChannel()
                : channelPolicy.primaryChannel();
        return NotificationAttempt.created(
                AttemptId.newId(),
                request.notificationId(),
                nextAttemptNumber,
                dispatchChannel.name(),
                request.payloadJson(),
                now);
    }

    public void markAttemptSent(NotificationAttempt attempt, String providerRef, Long latencyMs, String providerResponse, Instant now) {
        attempt.markSent(providerRef, latencyMs, providerResponse);
        request.markSent(now);
        domainEvents.add(new NotificationDeliveryRecorded(
                request.notificationId().value(),
                request.organizationId().value(),
                attempt.attemptId().value(),
                attempt.providerCode(),
                attempt.providerRef(),
                now));
    }

    public void markAttemptFailed(
            NotificationAttempt attempt,
            String errorCode,
            String errorMessage,
            boolean retryable,
            Instant nextRetryAt,
            Long latencyMs,
            String providerResponse,
            Instant now) {
        attempt.markFailed(errorCode, errorMessage, retryable, latencyMs, providerResponse);
        if (!retryable || request.reachedMaxAttempts()) {
            request.discard(now);
            return;
        }
        request.markFailed(true, nextRetryAt, now);
    }

    public void discard(Instant now) {
        request.discard(now);
    }

    public void reconcileDeliveryByCallback(String providerCode, String providerRef, Instant now) {
        request.markSent(now);
        domainEvents.add(new NotificationDeliveryRecorded(
                request.notificationId().value(),
                request.organizationId().value(),
                "callback",
                providerCode,
                providerRef,
                now));
    }

    public NotificationRequest request() {
        return request;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
