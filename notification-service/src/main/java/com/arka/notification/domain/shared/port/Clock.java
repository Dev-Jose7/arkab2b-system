package com.arka.notification.domain.shared.port;

import java.time.Instant;

public interface Clock {

    Instant now();
}
