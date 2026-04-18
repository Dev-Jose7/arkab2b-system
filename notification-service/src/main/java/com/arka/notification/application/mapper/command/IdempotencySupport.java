package com.arka.notification.application.mapper.command;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class IdempotencySupport {

    private IdempotencySupport() {
    }

    public static String payloadHash(String payload) {
        String safePayload = payload == null ? "" : payload;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(safePayload.getBytes(StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                output.append(String.format("%02x", value));
            }
            return output.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }
}
