package com.arka.notification.infrastructure.adapter.out.persistence.repository;

import com.arka.notification.infrastructure.adapter.out.persistence.entity.NotificationTemplateRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveNotificationTemplateRepository extends ReactiveCrudRepository<NotificationTemplateRow, String> {

    @Query("""
            SELECT *
            FROM notification_templates
            WHERE organization_id = :organizationId
              AND source_event_type = :sourceEventType
              AND channel = :channel
              AND active = TRUE
            ORDER BY template_version DESC
            LIMIT 1
            """)
    Mono<NotificationTemplateRow> findActive(String organizationId, String sourceEventType, String channel);
}
