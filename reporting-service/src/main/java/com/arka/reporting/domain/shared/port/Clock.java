package com.arka.reporting.domain.shared.port;

import java.time.Instant;

public interface Clock {

    Instant now();
}
