package com.arka.directory.infrastructure.adapter.out.external;

import com.arka.directory.application.port.out.external.ClockPort;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemClockAdapter implements ClockPort, com.arka.directory.domain.shared.port.Clock {

    private final Clock clock;

    public SystemClockAdapter() {
        this.clock = Clock.systemUTC();
    }

    @Override
    public Instant now() {
        return Instant.now(clock);
    }
}
