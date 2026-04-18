package com.arka.inventory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("warehouses")
public record WarehouseRow(
        @Id @Column("warehouse_id") String warehouseId,
        @Column("organization_id") String organizationId,
        @Column("warehouse_code") String warehouseCode,
        @Column("warehouse_name") String warehouseName,
        @Column("country_code") String countryCode,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
