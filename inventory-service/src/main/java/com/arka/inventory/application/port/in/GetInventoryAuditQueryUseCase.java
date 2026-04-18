package com.arka.inventory.application.port.in;

import com.arka.inventory.application.query.GetInventoryAuditQuery;
import com.arka.inventory.application.result.InventoryAuditResult;
import reactor.core.publisher.Mono;

public interface GetInventoryAuditQueryUseCase {

    Mono<InventoryAuditResult> handle(GetInventoryAuditQuery query);
}
