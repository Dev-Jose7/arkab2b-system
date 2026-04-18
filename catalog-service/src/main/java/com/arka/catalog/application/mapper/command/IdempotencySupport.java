package com.arka.catalog.application.mapper.command;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class IdempotencySupport {

    private IdempotencySupport() {
    }

    public static String payloadHash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to compute payload hash", exception);
        }
    }
}
