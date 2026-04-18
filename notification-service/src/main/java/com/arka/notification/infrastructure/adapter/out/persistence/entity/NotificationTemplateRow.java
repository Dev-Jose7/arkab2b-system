package com.arka.notification.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("notification_templates")
public record NotificationTemplateRow(
        @Id
        @Column("template_id") String templateId,
        @Column("organization_id") String organizationId,
        @Column("source_event_type") String sourceEventType,
        @Column("channel") String channel,
        @Column("template_version") Integer templateVersion,
        @Column("subject_template") String subjectTemplate,
        @Column("body_template") String bodyTemplate,
        @Column("active") Boolean active,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}
