package com.arka.catalog.application.port.out.external;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}
