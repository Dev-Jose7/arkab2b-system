package com.arka.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("permission_catalog")
public record PermissionCatalogRow(
        @Id @Column("permission_id") String permissionId,
        @Column("permission_code") String permissionCode,
        @Column("resource") String resource,
        @Column("action") String action,
        @Column("scope") String scope,
        @Column("display_name") String displayName,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
