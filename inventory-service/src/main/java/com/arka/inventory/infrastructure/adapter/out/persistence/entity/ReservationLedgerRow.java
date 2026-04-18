package com.arka.inventory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("reservation_ledgers")
public record ReservationLedgerRow(
        @Id @Column("ledger_id") String ledgerId,
        @Column("tenant_id") String tenantId,
        @Column("reservation_id") String reservationId,
        @Column("entry_type") String entryType,
        @Column("qty") Integer qty,
        @Column("note") String note,
        @Column("created_at") Instant createdAt) {}
