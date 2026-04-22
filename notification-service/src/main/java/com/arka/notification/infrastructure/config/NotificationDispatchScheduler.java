package com.arka.notification.infrastructure.config;

import com.arka.notification.application.command.DispatchNotificationCommand;
import com.arka.notification.application.port.in.DispatchNotificationCommandUseCase;
import com.arka.notification.application.port.out.external.ClockPort;
import com.arka.notification.application.port.out.persistence.NotificationRequestPersistencePort;
import com.arka.notification.infrastructure.adapter.in.security.IamSecurityPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class NotificationDispatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchScheduler.class);

    private final NotificationRequestPersistencePort notificationRequestPersistencePort;
    private final DispatchNotificationCommandUseCase dispatchNotificationCommandUseCase;
    private final ClockPort clockPort;

    @Value("${app.notification.dispatch.scheduler.enabled:true}")
    private boolean enabled;

    @Value("${app.notification.dispatch.scheduler.batch-size:50}")
    private int batchSize;

    @Value("${app.notification.dispatch.scheduler.actor-id:notification-scheduler}")
    private String schedulerActorId;

    public NotificationDispatchScheduler(
            NotificationRequestPersistencePort notificationRequestPersistencePort,
            DispatchNotificationCommandUseCase dispatchNotificationCommandUseCase,
            ClockPort clockPort) {
        this.notificationRequestPersistencePort = notificationRequestPersistencePort;
        this.dispatchNotificationCommandUseCase = dispatchNotificationCommandUseCase;
        this.clockPort = clockPort;
    }

    @Scheduled(fixedDelayString = "${app.notification.dispatch.scheduler.poll-interval-ms:5000}")
    public void dispatchPendingNotifications() {
        if (!enabled) {
            return;
        }
        notificationRequestPersistencePort
                .findDispatchable(clockPort.now(), Math.max(1, batchSize))
                .concatMap(request -> dispatchNotificationCommandUseCase
                        .handle(new DispatchNotificationCommand(
                                request.organizationId().value(),
                                schedulerActorId,
                                request.notificationId().value(),
                                null))
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                technicalAuthentication(request.organizationId().value())))
                        .doOnError(throwable -> log.warn(
                                "Notification scheduler dispatch failed notificationId={} organizationId={} error={}",
                                request.notificationId().value(),
                                request.organizationId().value(),
                                throwable.getMessage()))
                        .onErrorResume(throwable -> Mono.empty()))
                .then()
                .subscribe();
    }

    private Authentication technicalAuthentication(String organizationId) {
        IamSecurityPrincipal principal = new IamSecurityPrincipal(
                schedulerActorId,
                organizationId,
                "",
                java.util.Set.of("ROLE_INTERNAL_ACTOR"));
        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                java.util.Set.of(new SimpleGrantedAuthority("ROLE_INTERNAL_ACTOR")));
    }
}
