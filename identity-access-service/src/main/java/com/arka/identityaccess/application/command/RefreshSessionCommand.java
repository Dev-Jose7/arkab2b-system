package com.arka.identityaccess.application.command;

public record RefreshSessionCommand(
        String refreshToken,
        String ipAddress,
        String organizationId,
        String countryCode) {

    public RefreshSessionCommand(String refreshToken) {
        this(refreshToken, "127.0.0.1", null, null);
    }
}
