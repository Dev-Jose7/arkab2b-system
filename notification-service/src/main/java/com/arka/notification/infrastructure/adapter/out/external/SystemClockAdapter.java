package com.arka.notification.infrastructure.adapter.out.external;

import com.arka.notification.application.port.out.external.ClockPort;
import com.arka.notification.domain.shared.port.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemClockAdapter implements ClockPort, Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
