package com.arka.catalog.domain.shared.port;

import java.time.Instant;

public interface Clock {

    Instant now();
}
