package com.arka.order.application.command;

public record HandleReservationExpiredCommand(
        String tenantId,
        String organizationId,
        String cartId,
        String reservationId,
        String eventId,
        String actorUserId) {}
