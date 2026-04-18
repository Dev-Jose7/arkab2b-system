package com.arka.directory.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("organization_user_profile")
public record OrganizationUserProfileRow(
        @Id @Column("user_profile_id") String userProfileId,
        @Column("organization_id") String organizationId,
        @Column("iam_user_id") String iamUserId,
        @Column("display_name") String displayName,
        @Column("job_title") String jobTitle,
        @Column("department") String department,
        @Column("locale") String locale,
        @Column("timezone") String timezone,
        @Column("role_reference") String roleReference,
        @Column("ownership_scope") String ownershipScope,
        @Column("status") String status,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
