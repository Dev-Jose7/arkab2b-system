package com.arka.notification.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arka.notification.application.port.in.DispatchNotificationCommandUseCase;
import com.arka.notification.application.port.out.external.ClockPort;
import com.arka.notification.application.port.out.persistence.NotificationRequestPersistencePort;
import com.arka.notification.application.result.NotificationDetailResult;
import com.arka.notification.domain.notificationdispatch.entity.NotificationRequest;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationChannel;
import com.arka.notification.domain.notificationdispatch.enumtype.NotificationRequestStatus;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationId;
import com.arka.notification.domain.notificationdispatch.valueobject.NotificationKey;
import com.arka.notification.domain.notificationdispatch.valueobject.OrganizationId;
import com.arka.notification.infrastructure.adapter.in.security.IamSecurityPrincipal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchSchedulerTest {

    @Mock
    private NotificationRequestPersistencePort notificationRequestPersistencePort;

    @Mock
    private DispatchNotificationCommandUseCase dispatchNotificationCommandUseCase;

    @Mock
    private ClockPort clockPort;

    private NotificationDispatchScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NotificationDispatchScheduler(
                notificationRequestPersistencePort,
                dispatchNotificationCommandUseCase,
                clockPort);
        ReflectionTestUtils.setField(scheduler, "enabled", true);
        ReflectionTestUtils.setField(scheduler, "batchSize", 50);
        ReflectionTestUtils.setField(scheduler, "schedulerActorId", "notification-scheduler");
    }

    @Test
    void shouldPropagateTrustedServiceAuthenticationWhenDispatchingPendingNotifications() {
        Instant now = Instant.parse("2026-04-19T20:00:00Z");
        NotificationRequest request = NotificationRequest.rehydrate(
                NotificationId.of("notif-1"),
                OrganizationId.of("organization-phase6"),
                "evt-1",
                "CartCreated",
                "organization-phase6",
                NotificationChannel.EMAIL,
                NotificationKey.of("organization-phase6:CartCreated:evt-1"),
                "tpl-cart-created-v1-ph6",
                "policy-cart-created-v1-ph6",
                "{\"payload\":true}",
                NotificationRequestStatus.PENDING,
                true,
                now,
                3,
                0,
                "trace-1",
                "corr-1",
                0L,
                now,
                now);

        when(clockPort.now()).thenReturn(now);
        when(notificationRequestPersistencePort.findDispatchable(now, 50)).thenReturn(Flux.just(request));
        when(dispatchNotificationCommandUseCase.handle(any()))
                .thenAnswer(invocation -> ReactiveSecurityContextHolder.getContext()
                        .map(context -> {
                            IamSecurityPrincipal principal =
                                    IamSecurityPrincipal.fromAuthentication(context.getAuthentication());
                            assertEquals("notification-scheduler", principal.actorId());
                            assertEquals("organization-phase6", principal.organizationId());
                            return new NotificationDetailResult(null, List.of(), List.of());
                        }));

        scheduler.dispatchPendingNotifications();
    }
}
