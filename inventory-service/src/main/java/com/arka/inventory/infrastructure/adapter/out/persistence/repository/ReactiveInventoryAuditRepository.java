package com.arka.inventory.infrastructure.adapter.out.persistence.repository;

import com.arka.inventory.infrastructure.adapter.out.persistence.entity.InventoryAuditRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveInventoryAuditRepository extends ReactiveCrudRepository<InventoryAuditRow, String> {

    @Modifying
    @Query("""
            INSERT INTO inventory_audits (
                audit_id,
                organization_id,
                actor_user_id,
                action_type,
                target_type,
                target_id,
                outcome,
                payload,
                created_at
            ) VALUES (
                :auditId,
                :organizationId,
                :actorUserId,
                :actionType,
                :targetType,
                :targetId,
                :outcome,
                CAST(:payload AS jsonb),
                :createdAt
            )
            """)
    Mono<Integer> insert(
            @Param("auditId") String auditId,
            @Param("organizationId") String organizationId,
            @Param("actorUserId") String actorUserId,
            @Param("actionType") String actionType,
            @Param("targetType") String targetType,
            @Param("targetId") String targetId,
            @Param("outcome") String outcome,
            @Param("payload") String payload,
            @Param("createdAt") Instant createdAt);

    @Query("""
            SELECT audit_id, organization_id, actor_user_id, action_type, target_type, target_id, outcome, payload, created_at
            FROM inventory_audits
            WHERE organization_id = :organizationId
            ORDER BY created_at DESC
            LIMIT :limit
            """)
    Flux<InventoryAuditRow> findByOrganization(@Param("organizationId") String organizationId, @Param("limit") int limit);
}
