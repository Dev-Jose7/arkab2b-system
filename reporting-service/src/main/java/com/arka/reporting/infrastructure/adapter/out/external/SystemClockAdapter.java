package com.arka.reporting.infrastructure.adapter.out.external;

import com.arka.reporting.application.port.out.external.ClockPort;
import com.arka.reporting.domain.shared.port.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemClockAdapter implements ClockPort, Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
