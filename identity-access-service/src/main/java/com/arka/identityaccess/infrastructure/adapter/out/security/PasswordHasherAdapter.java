package com.arka.identityaccess.infrastructure.adapter.out.security;

import com.arka.identityaccess.application.port.out.security.PasswordHashPort;
import com.arka.identityaccess.domain.shared.port.PasswordHasher;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasherAdapter implements PasswordHasher {

    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

    private final PasswordHashPort passwordHashPort;

    public PasswordHasherAdapter(PasswordHashPort passwordHashPort) {
        this.passwordHashPort = passwordHashPort;
    }

    @Override
    public String hash(String rawPassword) {
        String hashed = passwordHashPort.hash(rawPassword).block(BLOCK_TIMEOUT);
        if (hashed == null || hashed.isBlank()) {
            throw new IllegalStateException("Password hashing produced empty output");
        }
        return hashed;
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        Boolean matched = passwordHashPort.matches(rawPassword, passwordHash).block(BLOCK_TIMEOUT);
        return Boolean.TRUE.equals(matched);
    }
}
