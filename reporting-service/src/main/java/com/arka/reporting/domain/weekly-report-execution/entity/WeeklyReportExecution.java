package com.arka.reporting.domain.weeklyreportexecution.entity;

import com.arka.reporting.domain.weeklyreportexecution.enumtype.ReportType;
import com.arka.reporting.domain.weeklyreportexecution.enumtype.WeeklyReportExecutionStatus;
import com.arka.reporting.domain.weeklyreportexecution.exception.WeeklyReportExecutionArtifactRequiredException;
import com.arka.reporting.domain.weeklyreportexecution.exception.WeeklyReportExecutionTransitionException;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.ExecutionId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.OrganizationId;
import com.arka.reporting.domain.weeklyreportexecution.valueobject.WeekId;
import java.time.Instant;

public final class WeeklyReportExecution {

    private final ExecutionId executionId;
    private final OrganizationId organizationId;
    private final WeekId weekId;
    private final ReportType reportType;
    private final Instant createdAt;

    private WeeklyReportExecutionStatus status;
    private String errorCode;
    private String errorMessage;
    private String completionArtifactRef;
    private long version;
    private Instant startedAt;
    private Instant completedAt;
    private Instant updatedAt;

    private WeeklyReportExecution(
            ExecutionId executionId,
            OrganizationId organizationId,
            WeekId weekId,
            ReportType reportType,
            WeeklyReportExecutionStatus status,
            String errorCode,
            String errorMessage,
            String completionArtifactRef,
            long version,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            Instant updatedAt) {
        this.executionId = executionId;
        this.organizationId = organizationId;
        this.weekId = weekId;
        this.reportType = reportType;
        this.status = status;
        this.errorCode = errorCode == null ? "" : errorCode.trim();
        this.errorMessage = errorMessage == null ? "" : errorMessage.trim();
        this.completionArtifactRef = completionArtifactRef == null ? "" : completionArtifactRef.trim();
        this.version = Math.max(version, 0L);
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
    }

    public static WeeklyReportExecution pending(
            OrganizationId organizationId,
            WeekId weekId,
            ReportType reportType,
            Instant now) {
        return new WeeklyReportExecution(
                ExecutionId.newId(),
                organizationId,
                weekId,
                reportType,
                WeeklyReportExecutionStatus.PENDING,
                "",
                "",
                "",
                0L,
                now,
                null,
                null,
                now);
    }

    public static WeeklyReportExecution rehydrate(
            ExecutionId executionId,
            OrganizationId organizationId,
            WeekId weekId,
            ReportType reportType,
            WeeklyReportExecutionStatus status,
            String errorCode,
            String errorMessage,
            String completionArtifactRef,
            long version,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            Instant updatedAt) {
        return new WeeklyReportExecution(
                executionId,
                organizationId,
                weekId,
                reportType,
                status,
                errorCode,
                errorMessage,
                completionArtifactRef,
                version,
                createdAt,
                startedAt,
                completedAt,
                updatedAt);
    }

    public void start(Instant now) {
        if (status != WeeklyReportExecutionStatus.PENDING) {
            throw new WeeklyReportExecutionTransitionException(status.name(), WeeklyReportExecutionStatus.RUNNING.name());
        }
        status = WeeklyReportExecutionStatus.RUNNING;
        startedAt = now;
        updatedAt = now;
    }

    public void complete(String locationRef, Instant now) {
        if (status != WeeklyReportExecutionStatus.RUNNING) {
            throw new WeeklyReportExecutionTransitionException(status.name(), WeeklyReportExecutionStatus.COMPLETED.name());
        }
        if (locationRef == null || locationRef.isBlank()) {
            throw new WeeklyReportExecutionArtifactRequiredException();
        }
        status = WeeklyReportExecutionStatus.COMPLETED;
        completionArtifactRef = locationRef.trim();
        completedAt = now;
        updatedAt = now;
        errorCode = "";
        errorMessage = "";
    }

    public void fail(String errorCode, String errorMessage, Instant now) {
        if (status != WeeklyReportExecutionStatus.RUNNING) {
            throw new WeeklyReportExecutionTransitionException(status.name(), WeeklyReportExecutionStatus.FAILED.name());
        }
        status = WeeklyReportExecutionStatus.FAILED;
        this.errorCode = errorCode == null ? "reporte_generacion_fallida" : errorCode.trim();
        this.errorMessage = errorMessage == null ? "" : errorMessage.trim();
        completedAt = now;
        updatedAt = now;
    }

    public void bumpVersion() {
        this.version = this.version + 1;
    }

    public ExecutionId executionId() {
        return executionId;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public WeekId weekId() {
        return weekId;
    }

    public ReportType reportType() {
        return reportType;
    }

    public WeeklyReportExecutionStatus status() {
        return status;
    }

    public String errorCode() {
        return errorCode;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public String completionArtifactRef() {
        return completionArtifactRef;
    }

    public long version() {
        return version;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
