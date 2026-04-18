package com.arka.notification.application.port.out.persistence;

import com.arka.notification.domain.notificationdispatch.entity.ChannelPolicy;
import com.arka.notification.domain.notificationdispatch.entity.NotificationTemplate;
import reactor.core.publisher.Mono;

public interface NotificationTemplatePolicyPersistencePort {

    Mono<NotificationTemplate> findActiveTemplate(String organizationId, String sourceEventType, String channel);

    Mono<ChannelPolicy> findActivePolicy(String organizationId, String sourceEventType);
}
