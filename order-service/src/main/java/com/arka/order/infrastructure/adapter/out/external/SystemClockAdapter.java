package com.arka.order.infrastructure.adapter.out.external;

import com.arka.order.application.port.out.external.ClockPort;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SystemClockAdapter implements ClockPort {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
