package com.arka.notification.infrastructure.adapter.out.persistence.repository;

import com.arka.notification.infrastructure.adapter.out.persistence.entity.NotificationAttemptRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReactiveNotificationAttemptRepository extends ReactiveCrudRepository<NotificationAttemptRow, String> {

    @Query("""
            SELECT *
            FROM notification_attempts
            WHERE organization_id = :organizationId
              AND notification_id = :notificationId
            ORDER BY attempt_number ASC
            """)
    Flux<NotificationAttemptRow> findByNotificationId(String organizationId, String notificationId);
}
