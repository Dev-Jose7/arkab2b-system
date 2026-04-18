package com.arka.reporting.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("sales_projections")
public record SalesProjectionRow(
        @Id
        @Column("projection_id") String projectionId,
        @Column("organization_id") String organizationId,
        @Column("period") String period,
        @Column("total_sales") BigDecimal totalSales,
        @Column("paid_amount") BigDecimal paidAmount,
        @Column("pending_amount") BigDecimal pendingAmount,
        @Column("confirmed_orders") Long confirmedOrders,
        @Column("average_ticket") BigDecimal averageTicket,
        @Column("version") Long version,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {
}
