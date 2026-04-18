package com.arka.directory.application.command;

public record MarkDefaultAddressCommand(
        String organizationId,
        String addressId,
        String addressType,
        String actorUserId,
        String actorOrganizationId) {}
