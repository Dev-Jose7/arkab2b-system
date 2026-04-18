package com.arka.inventory.application.port.out.external;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}
