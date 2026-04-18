package com.arka.inventory.domain.inventorybalance.event;

import java.time.Instant;

public final class InventoryMutationEvent extends AbstractInventoryDomainEvent {

    private final String organizationId;
    private final String mutationType;
    private final String targetType;
    private final String targetId;
    private final String actorUserId;

    public InventoryMutationEvent(
            Instant occurredAt,
            String aggregateId,
            String organizationId,
            String mutationType,
            String targetType,
            String targetId,
            String actorUserId) {
        super(mutationType, occurredAt, aggregateId, targetType);
        this.organizationId = organizationId;
        this.mutationType = mutationType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.actorUserId = actorUserId;
    }

    public String organizationId() {
        return organizationId;
    }

    public String mutationType() {
        return mutationType;
    }

    public String targetType() {
        return targetType;
    }

    public String targetId() {
        return targetId;
    }

    public String actorUserId() {
        return actorUserId;
    }
}
