package com.arka.catalog.infrastructure.adapter.out.external;

import com.arka.catalog.application.port.out.external.ClockPort;
import com.arka.catalog.domain.shared.port.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemClockAdapter implements ClockPort, Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
