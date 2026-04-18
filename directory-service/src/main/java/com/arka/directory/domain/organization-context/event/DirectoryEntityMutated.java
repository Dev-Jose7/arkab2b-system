package com.arka.directory.domain.organizationcontext.event;

import com.arka.directory.domain.countrypolicy.event.AbstractDirectoryDomainEvent;
import java.time.Instant;

public final class DirectoryEntityMutated extends AbstractDirectoryDomainEvent {

    private final String mutationType;
    private final String targetType;
    private final String targetId;
    private final String actorUserId;

    public DirectoryEntityMutated(
            Instant occurredAt,
            String organizationId,
            String mutationType,
            String targetType,
            String targetId,
            String actorUserId) {
        super(mutationType, occurredAt, organizationId, targetType);
        this.mutationType = mutationType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.actorUserId = actorUserId;
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
