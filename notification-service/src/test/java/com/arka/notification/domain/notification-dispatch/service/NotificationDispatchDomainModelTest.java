package com.arka.notification.domain.notificationdispatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.notification.domain.notificationdispatch.aggregate.NotificationDispatch;
import com.arka.notification.domain.notificationdispatch.entity.ChannelPolicy;
import com.arka.notification.domain.notificationdispatch.entity.NotificationAttempt;
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.notificationdispatch.exception.DiscardedNotificationCannotDispatchException;
import com.arka.notification.domain.notificationdispatch.exception.NotificationTerminalStateException;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.RelevantChangeNotification;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationDispatchDomainModelTest {

    @Test
    void shouldEmitNotificationInPendingAndCreateDomainEvent() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        RelevantChangeNotification change = new RelevantChangeNotification(
                OrganizationId.of("organization-demo"),
                "evt-1",
                "order.confirmed",
                "recipient-1",
                NotificationChannel.EMAIL,
                "{}",
                "trace-1",
                "corr-1");

        NotificationDispatch dispatch = NotificationDispatch.emit(
                change,
                "template-1",
                "policy-1",
                "{}",
                3,
                now,
                now);

        assertEquals("PENDING", dispatch.request().status().name());
        assertTrue(dispatch.request().retryable());
        assertEquals(1, dispatch.pullDomainEvents().size());
    }

    @Test
    void shouldKeepSentAsTerminalState() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        NotificationRequest request = pendingRequest(now);
        ChannelPolicy policy = policy();

        NotificationDispatch dispatch = NotificationDispatch.rehydrate(request);
        NotificationAttempt attempt = dispatch.beginDispatchAttempt(policy, now);
        dispatch.markAttemptSent(attempt, "provider-ref", 120L, "{}", now.plusSeconds(1));

        assertEquals("SENT", dispatch.request().status().name());
        assertFalse(dispatch.request().retryable());
        assertThrows(
                NotificationTerminalStateException.class,
                () -> dispatch.request().markFailed(true, now.plusSeconds(20), now.plusSeconds(2)));
    }

    @Test
    void shouldRejectDispatchWhenNotificationIsDiscarded() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        NotificationRequest request = pendingRequest(now);
        request.discard(now.plusSeconds(2));

        NotificationDispatch dispatch = NotificationDispatch.rehydrate(request);

        assertThrows(
                DiscardedNotificationCannotDispatchException.class,
                () -> dispatch.beginDispatchAttempt(policy(), now.plusSeconds(3)));
    }

    @Test
    void shouldIncrementAttemptNumberSequentiallyPerRequest() {
        Instant now = Instant.parse("2026-04-01T00:00:00Z");
        NotificationRequest request = pendingRequest(now);
        NotificationDispatch dispatch = NotificationDispatch.rehydrate(request);

        NotificationAttempt firstAttempt = dispatch.beginDispatchAttempt(policy(), now);
        dispatch.markAttemptFailed(
                firstAttempt,
                "PROVIDER_TIMEOUT",
                "timeout",
                true,
                now.plusSeconds(60),
                1500L,
                "{}",
                now.plusSeconds(1));

        NotificationAttempt secondAttempt = dispatch.beginDispatchAttempt(policy(), now.plusSeconds(61));

        assertEquals(1, firstAttempt.attemptNumber());
        assertEquals(2, secondAttempt.attemptNumber());
        assertEquals(2, dispatch.request().attemptCount());
        assertEquals("SMS", secondAttempt.providerCode());
    }

    @Test
    void shouldBuildDedupeKeyFromEventRecipientAndChannel() {
        NotificationKey key = NotificationKey.fromEventRecipientAndChannel(
                "evt-99",
                "recipient-x",
                NotificationChannel.WHATSAPP);

        assertEquals("evt-99::recipient-x::WHATSAPP", key.value());
    }

    private NotificationRequest pendingRequest(Instant now) {
        return NotificationRequest.createPending(
                NotificationId.of("noti-1"),
                OrganizationId.of("organization-demo"),
                "evt-1",
                "order.confirmed",
                "recipient-1",
                NotificationChannel.EMAIL,
                NotificationKey.fromEventRecipientAndChannel("evt-1", "recipient-1", NotificationChannel.EMAIL),
                "template-1",
                "policy-1",
                "{}",
                3,
                now,
                "trace-1",
                "corr-1",
                now);
    }

    private ChannelPolicy policy() {
        return new ChannelPolicy(
                "policy-1",
                "organization-demo",
                "order.confirmed",
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                3,
                60,
                true);
    }
}
