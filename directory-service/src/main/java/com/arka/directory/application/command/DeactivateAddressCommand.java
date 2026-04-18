package com.arka.directory.application.command;

public record DeactivateAddressCommand(
        String organizationId,
        String addressId,
        String actorUserId,
        String actorOrganizationId) {}
