package com.arka.reporting.domain.weeklyreportexecution.exception;

public class WeeklyReportExecutionArtifactRequiredException extends WeeklyReportExecutionDomainException {

    public WeeklyReportExecutionArtifactRequiredException() {
        super("artifact_location_requerido", "COMPLETED requiere al menos un artifact con locationRef");
    }
}
