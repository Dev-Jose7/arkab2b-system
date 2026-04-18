package com.arka.notification.infrastructure.config;

import com.arka.notification.application.command.DispatchNotificationCommand;
import com.arka.notification.application.port.in.DispatchNotificationCommandUseCase;
import com.arka.notification.application.port.out.external.ClockPort;
import com.arka.notification.application.port.out.persistence.NotificationRequestPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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
                                request.tenantId().value(),
                                schedulerActorId,
                                request.notificationId().value(),
                                null))
                        .doOnError(throwable -> log.warn(
                                "Notification scheduler dispatch failed notificationId={} tenantId={} error={}",
                                request.notificationId().value(),
                                request.tenantId().value(),
                                throwable.getMessage()))
                        .onErrorResume(throwable -> Mono.empty()))
                .then()
                .subscribe();
    }
}
