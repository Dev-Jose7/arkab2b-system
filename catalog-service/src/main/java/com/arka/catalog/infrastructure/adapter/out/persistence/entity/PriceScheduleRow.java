package com.arka.catalog.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("price_schedules")
public record PriceScheduleRow(
        @Id
        @Column("schedule_id") String scheduleId,
        @Column("tenant_id") String tenantId,
        @Column("price_id") String priceId,
        @Column("execute_after") Instant executeAfter,
        @Column("job_status") String jobStatus,
        @Column("error_message") String errorMessage,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}
