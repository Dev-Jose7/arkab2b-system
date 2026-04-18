package com.arka.inventory.application.mapper.command;

import com.arka.inventory.application.exception.InventoryValidationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class IdempotencySupport {

    private IdempotencySupport() {}

    public static String normalizeKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InventoryValidationException("Idempotency-Key header is required for write operation");
        }
        return idempotencyKey.trim();
    }

    public static String sha256(String rawValue) {
        String payload = rawValue == null ? "" : rawValue;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
