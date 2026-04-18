package com.arka.reporting.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("consumer_checkpoints")
public record ConsumerCheckpointRow(
        @Id
        @Column("checkpoint_id") String checkpointId,
        @Column("organization_id") String organizationId,
        @Column("consumer_name") String consumerName,
        @Column("topic") String topic,
        @Column("partition") Integer partition,
        @Column("current_offset") Long currentOffset,
        @Column("latest_offset") Long latestOffset,
        @Column("lag") Long lag,
        @Column("updated_at") Instant updatedAt) {
}
