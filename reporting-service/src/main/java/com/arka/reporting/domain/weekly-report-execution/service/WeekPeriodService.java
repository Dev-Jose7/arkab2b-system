package com.arka.reporting.domain.weeklyreportexecution.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import org.springframework.stereotype.Component;

@Component
public class WeekPeriodService {

    public String weekIdFrom(Instant instant) {
        var date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        int week = date.get(WeekFields.ISO.weekOfWeekBasedYear());
        int year = date.get(WeekFields.ISO.weekBasedYear());
        return String.format("%04d-W%02d", year, week);
    }
}
