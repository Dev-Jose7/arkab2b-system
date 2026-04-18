package com.arka.notification.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arka.notification.domain.notificationdispatch.entity.NotificationAttempt;
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.notificationdispatch.valueobject.AttemptId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
import com.arka.notification.infrastructure.adapter.out.persistence.mapper.NotificationRowMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationRowMapperTest {

    private final NotificationRowMapper mapper = new NotificationRowMapper();

    @Test
    void shouldRoundTripNotificationRequestBetweenDomainAndRow() {
        Instant now = Instant.parse("2026-04-05T10:00:00Z");
        NotificationRequest request = NotificationRequest.createPending(
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

        NotificationRequest mapped = mapper.toDomain(mapper.toRow(request));

        assertEquals(request.notificationId().value(), mapped.notificationId().value());
        assertEquals(request.organizationId().value(), mapped.organizationId().value());
        assertEquals(request.status(), mapped.status());
        assertEquals(request.notificationKey().value(), mapped.notificationKey().value());
    }

    @Test
    void shouldMapNotificationAttemptBetweenDomainAndRow() {
        Instant now = Instant.parse("2026-04-05T10:00:00Z");
        NotificationAttempt attempt = NotificationAttempt.created(
                AttemptId.of("att-1"),
                NotificationId.of("noti-1"),
                1,
                "provider-x",
                "{}",
                now);
        attempt.markSent("provider-ref-1", 100L, "{\"status\":\"sent\"}");

        NotificationAttempt mapped = mapper.toDomain(mapper.toRow(attempt, "organization-demo"));

        assertEquals("att-1", mapped.attemptId().value());
        assertEquals("SENT", mapped.resultStatus().name());
        assertEquals("provider-ref-1", mapped.providerRef());
    }
}
