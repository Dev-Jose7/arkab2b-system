package com.arka.inventory.application.port.out.audit;

import com.arka.inventory.application.result.InventoryAuditEntryResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryAuditPort {

    Mono<Void> record(
            String organizationId,
            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String outcome,
            String payload);

    Flux<InventoryAuditEntryResult> findByOrganization(String organizationId, int limit);
}
